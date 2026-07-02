// CourseRepository.kt
package com.lumina.app.data.repository

import com.lumina.app.data.model.*
import com.lumina.app.data.source.local.dao.*
import com.lumina.app.data.source.remote.DictionaryApiService
import com.lumina.app.data.source.remote.DictionaryEntry
import com.lumina.app.data.source.remote.ai.GeminiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CourseRepository(
    private val courseDao: CourseDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val vocabularyDao: VocabularyDao,
    private val topicGroupDao: TopicGroupDao? = null,
    private val dictionaryApiService: DictionaryApiService? = null,
    private val geminiService: GeminiService? = null,
    private val firestoreSync: FirestoreSyncManager? = null
) {
    companion object {
        private const val TAG = "CourseRepository"
    }

    // ── Firestore Sync Scope ──
    // Scope riêng, sống theo vòng đời của Repository (KHÔNG phụ thuộc vào
    // viewModelScope của bất kỳ Fragment/ViewModel nào gọi vào đây).
    // Nhờ vậy, nếu người dùng back/đóng màn hình ngay sau khi bấm Lưu,
    // việc đồng bộ lên Firestore vẫn tiếp tục chạy ngầm cho tới khi xong,
    // thay vì bị hủy giữa chừng theo Fragment.
    // SupervisorJob giúp 1 lần sync bị lỗi không làm hỏng/hủy các lần sync khác.
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun withFirestoreSync(action: suspend (FirestoreSyncManager, String) -> Unit) {
        val sync = firestoreSync ?: return
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid.isNullOrBlank()) {
            android.util.Log.w(TAG, "Firestore sync skipped: user not authenticated")
            return
        }
        syncScope.launch {
            try {
                action(sync, firebaseUid)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Firestore sync failed: ${e.message}", e)
            }
        }
    }

    // ── AI Services ──
    suspend fun explainWordWithAi(word: String, meaning: String): String? {
        return geminiService?.explainVocabulary(word, meaning)
    }

    suspend fun suggestVocabularyWithAi(word: String): String? {
        return geminiService?.suggestFullVocabulary(word)
    }

    suspend fun groupVocabularyWithAi(unitId: Long): Boolean {
        if (geminiService == null || topicGroupDao == null) return false

        try {
            val vocabList = vocabularyDao.getVocabularyByUnit(unitId).first()
            if (vocabList.isEmpty()) {
                android.util.Log.e("CourseRepository", "No vocabulary found for unit $unitId")
                return false
            }

            val wordStrings = vocabList.map { it.word }
            android.util.Log.d("CourseRepository", "Requesting AI to group ${wordStrings.size} words")

            val rawResult = geminiService.groupVocabularyByTopics(wordStrings)
            if (rawResult.isNullOrBlank()) {
                android.util.Log.e("CourseRepository", "AI returned empty response")
                return false
            }

            android.util.Log.d("CourseRepository", "AI Response: $rawResult")

            // Robust extraction: find the first '[' and last ']'
            val start = rawResult.indexOf('[')
            val end = rawResult.lastIndexOf(']')
            if (start == -1 || end == -1 || end <= start) {
                android.util.Log.e("CourseRepository", "Invalid JSON format from AI")
                return false
            }

            val cleanJson = rawResult.substring(start, end + 1)
            val jsonArray = JSONArray(cleanJson)

            // Xóa các nhóm cũ của Unit này trước khi tạo mới
            topicGroupDao.deleteGroupsByUnit(unitId)

            for (i in 0 until jsonArray.length()) {
                val groupObj = jsonArray.getJSONObject(i)
                val groupName = groupObj.getString("name")
                val wordsInGroup = groupObj.getJSONArray("words")

                val groupEntity = com.lumina.app.data.source.local.entity.TopicGroupEntity(
                    unitId = unitId,
                    name = groupName,
                    isAiGenerated = true,
                    createdAt = System.currentTimeMillis(),
                    orderIndex = i
                )

                val groupId = topicGroupDao.insertGroup(groupEntity)

                for (j in 0 until wordsInGroup.length()) {
                    val wordStr = wordsInGroup.getString(j)
                    val vocab = vocabList.find { it.word.equals(wordStr, ignoreCase = true) }
                    if (vocab != null) {
                        topicGroupDao.insertGroupWord(
                            com.lumina.app.data.source.local.entity.TopicGroupWordEntity(groupId, vocab.id)
                        )
                    }
                }
            }
            return true
        } catch (e: Exception) {
            android.util.Log.e("CourseRepository", "Error in groupVocabularyWithAi: ${e.message}", e)
            return false
        }
    }

    suspend fun renameTopicGroup(groupId: Long, newName: String) {
        topicGroupDao?.let { dao ->
            dao.getGroupById(groupId)?.let { group ->
                dao.updateGroup(group.copy(name = newName))
            }
        }
    }

    suspend fun deleteTopicGroup(groupId: Long) {
        topicGroupDao?.let { dao ->
            dao.getGroupById(groupId)?.let { group ->
                dao.deleteGroup(group)
            }
        }
    }

    suspend fun createTopicGroup(unitId: Long, name: String) {
        topicGroupDao?.let { dao ->
            dao.insertGroup(com.lumina.app.data.source.local.entity.TopicGroupEntity(
                unitId = unitId,
                name = name,
                isAiGenerated = false,
                createdAt = System.currentTimeMillis()
            ))
        }
    }

    suspend fun moveWordToTopicGroup(vocabId: Long, targetGroupId: Long, unitId: Long) {
        topicGroupDao?.let { dao ->
            // Bất kể từ đó đang ở nhóm nào trong Unit này, xóa hết đi để chuyển sang nhóm mới
            dao.removeWordFromAllGroupsInUnit(vocabId, unitId)
            dao.insertGroupWord(com.lumina.app.data.source.local.entity.TopicGroupWordEntity(targetGroupId, vocabId))
        }
    }

    suspend fun getTopicGroupsWithWords(unitId: Long): Flow<List<com.lumina.app.ui.unit.TopicGroupUiItem>> {
        if (topicGroupDao == null) return kotlinx.coroutines.flow.flowOf(emptyList())

        return topicGroupDao.getGroupsWithWordsByUnit(unitId).map { list ->
            list.map { item ->
                val vocabs = item.words.map { it.toModel() }

                // Calculate mastery rate (mock logic for now, should link to SRS)
                val masteredCount = vocabs.count { it.id % 3 == 0L }
                val rate = if (vocabs.isNotEmpty()) (masteredCount * 100) / vocabs.size else 0

                com.lumina.app.ui.unit.TopicGroupUiItem(
                    id = item.group.id,
                    unitId = item.group.unitId,
                    name = item.group.name,
                    description = "AI generated topics for better recall.",
                    words = vocabs,
                    masteryRate = rate,
                    isAiGenerated = item.group.isAiGenerated
                )
            }
        }
    }

    // ── Dictionary API ──
    suspend fun fetchDictionaryDetails(word: String): List<DictionaryEntry>? {
        return try {
            dictionaryApiService?.getWordDetails(word)
        } catch (e: Exception) {
            null
        }
    }

    // ── Course ──
    fun getCoursesByUser(userId: Long): Flow<List<Course>> =
        courseDao.getCoursesByUser(userId).map { list ->
            list.map { it.toModel() }
        }

    suspend fun getCourseById(id: Long): Course? =
        courseDao.getCourseById(id)?.toModel()

    suspend fun insertCourse(course: Course): Long {
        val id = courseDao.insertCourse(course.toEntity())
        if (id > 0) {
            withFirestoreSync { sync, uid ->
                sync.syncCourse(uid, course.copy(id = id))
            }
        }
        return id
    }

    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course.toEntity())
        withFirestoreSync { sync, uid ->
            sync.syncCourse(uid, course)
        }
    }

    suspend fun deleteCourse(course: Course) {
        courseDao.deleteCourse(course.toEntity())
        withFirestoreSync { sync, uid ->
            sync.deleteCourse(uid, course.id)
        }
    }

    fun getUnitsByCourse(courseId: Long): Flow<List<StudyUnit>> =
        unitDao.getUnitsByCourse(courseId).map { list ->
            list.map { it.toModel() }
        }

    suspend fun getUnitById(unitId: Long): StudyUnit? =
        unitDao.getUnitById(unitId)?.toModel()

    suspend fun insertUnit(unit: StudyUnit): Long {
        val id = unitDao.insertUnit(unit.toEntity())
        val finalUnit = unit.copy(id = id)
        
        // Launch sync in background scope immediately to avoid cancellation if UI closes
        withFirestoreSync { sync, uid ->
            getCourseById(unit.courseId)?.let { course ->
                sync.syncCourse(uid, course)
                sync.syncUnit(uid, course.id, finalUnit)
            }
        }
        return id
    }

    suspend fun updateUnit(unit: StudyUnit) {
        unitDao.updateUnit(unit.toEntity())
        withFirestoreSync { sync, uid ->
            getCourseById(unit.courseId)?.let { course ->
                sync.syncCourse(uid, course)
                sync.syncUnit(uid, course.id, unit)
            }
        }
    }

    suspend fun deleteUnit(unit: StudyUnit) {
        unitDao.deleteUnit(unit.toEntity())
        getCourseById(unit.courseId)?.let { course ->
            withFirestoreSync { sync, uid ->
                sync.deleteUnit(uid, course.id, unit.id)
            }
        }
    }

    suspend fun countUnitsByCourse(courseId: Long): Int =
        unitDao.countUnitsByCourse(courseId)

    // ── Lesson ──
    fun getLessonsByUnit(unitId: Long): Flow<List<Lesson>> =
        lessonDao.getLessonsByUnit(unitId).map { list ->
            list.map { it.toModel() }
        }

    suspend fun getLessonById(id: Long): Lesson? =
        lessonDao.getLessonById(id)?.toModel()

    suspend fun insertLesson(lesson: Lesson): Long {
        val id = lessonDao.insertLesson(lesson.toEntity())
        val finalLesson = lesson.copy(id = id)
        
        withFirestoreSync { sync, uid ->
            unitDao.getUnitById(lesson.unitId)?.toModel()?.let { u ->
                getCourseById(u.courseId)?.let { course ->
                    sync.syncUnit(uid, course.id, u)
                    sync.syncLesson(uid, course.id, u.id, finalLesson)
                }
            }
        }
        return id
    }

    suspend fun updateLesson(lesson: Lesson) {
        lessonDao.updateLesson(lesson.toEntity())
        withFirestoreSync { sync, uid ->
            unitDao.getUnitById(lesson.unitId)?.toModel()?.let { u ->
                getCourseById(u.courseId)?.let { course ->
                    sync.syncLesson(uid, course.id, u.id, lesson)
                }
            }
        }
    }

    suspend fun deleteLesson(lesson: Lesson) {
        lessonDao.deleteLesson(lesson.toEntity())
        val unit = unitDao.getUnitById(lesson.unitId)?.toModel()
        unit?.let { u ->
            getCourseById(u.courseId)?.let { course ->
                withFirestoreSync { sync, uid ->
                    sync.deleteLesson(uid, course.id, u.id, lesson.id)
                }
            }
        }
    }

    suspend fun countLessonsByUnit(unitId: Long): Int =
        lessonDao.countLessonsByUnit(unitId)

    suspend fun countLessonsByCourse(courseId: Long): Int =
        lessonDao.countLessonsByCourse(courseId)

    // ── Vocabulary ──
    fun getVocabularyByLesson(lessonId: Long): Flow<List<Vocabulary>> =
        vocabularyDao.getVocabularyByLesson(lessonId).map { list ->
            list.map { it.toModel() }
        }

    fun getVocabularyByTopicGroup(groupId: Long): Flow<List<Vocabulary>> =
        topicGroupDao?.getVocabularyByGroup(groupId)?.map { list ->
            list.map { it.toModel() }
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getVocabularyById(id: Long): Vocabulary? =
        vocabularyDao.getVocabularyById(id)?.toModel()

    suspend fun countVocabularyByLesson(lessonId: Long): Int =
        vocabularyDao.countByLesson(lessonId)

    suspend fun countVocabularyByUnit(unitId: Long): Int =
        vocabularyDao.countByUnit(unitId)

    suspend fun countVocabularyByCourse(courseId: Long): Int =
        vocabularyDao.countByCourse(courseId)

    suspend fun countLearnedVocabularyByLesson(lessonId: Long): Int =
        vocabularyDao.countLearnedByLesson(lessonId)

    suspend fun insertVocabulary(vocab: Vocabulary): Long {
        val id = vocabularyDao.insertVocabulary(vocab.toEntity())
        if (id > 0) {
            syncVocabToFirestore(vocab.copy(id = id))
        }
        return id
    }

    suspend fun insertVocabularyList(vocabs: List<Vocabulary>): List<Long> {
        val ids = vocabularyDao.insertVocabularyList(vocabs.map { it.toEntity() })
        vocabs.zip(ids).forEach { (v, id) ->
            if (id > 0) syncVocabToFirestore(v.copy(id = id))
        }
        return ids
    }

    suspend fun updateVocabulary(vocab: Vocabulary) {
        vocabularyDao.updateVocabulary(vocab.toEntity())
        syncVocabToFirestore(vocab)
    }

    suspend fun toggleFavorite(vocabId: Long, isFavorite: Boolean) {
        vocabularyDao.updateFavorite(vocabId, isFavorite)

        // Đồng bộ trạng thái yêu thích lên Firestore
        val vocab = vocabularyDao.getVocabularyById(vocabId)?.toModel()
        vocab?.let { syncVocabToFirestore(it) }
    }

    suspend fun deleteVocabulary(vocab: Vocabulary) {
        vocabularyDao.deleteVocabulary(vocab.toEntity())
        val lesson = lessonDao.getLessonById(vocab.lessonId)?.toModel()
        lesson?.let { l ->
            val unit = unitDao.getUnitById(l.unitId)?.toModel()
            unit?.let { u ->
                getCourseById(u.courseId)?.let { course ->
                    withFirestoreSync { sync, uid ->
                        sync.deleteVocabulary(uid, course.id, u.id, l.id, vocab.id)
                    }
                }
            }
        }
    }

    private fun syncVocabToFirestore(vocab: Vocabulary) {
        withFirestoreSync { sync, uid ->
            lessonDao.getLessonById(vocab.lessonId)?.toModel()?.let { l ->
                unitDao.getUnitById(l.unitId)?.toModel()?.let { u ->
                    getCourseById(u.courseId)?.let { course ->
                        // Đảm bảo toàn bộ cấu trúc cha-con đều có dữ liệu
                        sync.syncCourse(uid, course)
                        sync.syncUnit(uid, course.id, u)
                        sync.syncLesson(uid, course.id, u.id, l)
                        sync.syncVocabulary(uid, course.id, u.id, l.id, vocab)
                    }
                }
            }
        }
    }
}