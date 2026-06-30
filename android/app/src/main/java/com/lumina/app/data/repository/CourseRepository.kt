// CourseRepository.kt
package com.lumina.app.data.repository

import com.lumina.app.data.model.*
import com.lumina.app.data.source.local.dao.*
import com.lumina.app.data.source.remote.DictionaryApiService
import com.lumina.app.data.source.remote.DictionaryEntry
import com.lumina.app.data.source.remote.ai.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class CourseRepository(
    private val courseDao: CourseDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val vocabularyDao: VocabularyDao,
    private val topicGroupDao: TopicGroupDao? = null,
    private val dictionaryApiService: DictionaryApiService? = null,
    private val geminiService: GeminiService? = null
) {
    // ── AI Services ──
    suspend fun explainWordWithAi(word: String, meaning: String): String? {
        return geminiService?.explainVocabulary(word, meaning)
    }

    suspend fun suggestVocabularyWithAi(word: String): String? {
        return geminiService?.suggestFullVocabulary(word)
    }

    suspend fun groupVocabularyWithAi(unitId: Long): Boolean {
        if (geminiService == null || topicGroupDao == null) return false
        
        val vocabList = vocabularyDao.getVocabularyByUnit(unitId).first()
        if (vocabList.isEmpty()) return false
        
        val wordStrings = vocabList.map { it.word }
        val rawResult = geminiService.groupVocabularyByTopics(wordStrings) ?: return false
        
        try {
            // Robust extraction: find the first '[' and last ']'
            val start = rawResult.indexOf('[')
            val end = rawResult.lastIndexOf(']')
            if (start == -1 || end == -1 || end <= start) return false
            
            val cleanJson = rawResult.substring(start, end + 1)
            val jsonArray = JSONArray(cleanJson)
            
            // Xóa các nhóm cũ của Unit này trước khi tạo mới (nếu muốn refresh)
            topicGroupDao.deleteGroupsByUnit(unitId)
            
            for (i in 0 until jsonArray.length()) {
                val groupObj = jsonArray.getJSONObject(i)
                val groupName = groupObj.getString("name")
                val wordsInGroup = groupObj.getJSONArray("words")
                
                val groupEntity = com.lumina.app.data.source.local.entity.TopicGroupEntity(
                    unitId = unitId,
                    name = groupName,
                    isAiGenerated = true,
                    createdAt = System.currentTimeMillis()
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
            e.printStackTrace()
            return false
        }
    }

    suspend fun getTopicGroupsWithWords(unitId: Long): Flow<List<com.lumina.app.ui.unit.TopicGroupUiItem>> {
        if (topicGroupDao == null) return kotlinx.coroutines.flow.flowOf(emptyList())
        
        return topicGroupDao.getGroupsByUnit(unitId).map { groups ->
            groups.map { group ->
                val vocabEntities = topicGroupDao.getVocabularyByGroup(group.id).first()
                val vocabs = vocabEntities.map { it.toModel() }
                
                // Calculate mastery rate (mock logic for now, should link to SRS)
                val masteredCount = vocabs.count { it.id % 3 == 0L } 
                val rate = if (vocabs.isNotEmpty()) (masteredCount * 100) / vocabs.size else 0

                com.lumina.app.ui.unit.TopicGroupUiItem(
                    id = group.id,
                    unitId = group.unitId,
                    name = group.name,
                    description = "AI generated topics for better recall.",
                    words = vocabs,
                    masteryRate = rate,
                    isAiGenerated = group.isAiGenerated
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

    suspend fun insertCourse(course: Course) =
        courseDao.insertCourse(course.toEntity())

    suspend fun updateCourse(course: Course) =
        courseDao.updateCourse(course.toEntity())

    suspend fun deleteCourse(course: Course) =
        courseDao.deleteCourse(course.toEntity())

    // ── Unit ──
    fun getUnitsByCourse(courseId: Long): Flow<List<StudyUnit>> =
        unitDao.getUnitsByCourse(courseId).map { list ->
            list.map { it.toModel() }
        }

    suspend fun insertUnit(unit: StudyUnit) =
        unitDao.insertUnit(unit.toEntity())

    suspend fun updateUnit(unit: StudyUnit) =
        unitDao.updateUnit(unit.toEntity())

    suspend fun deleteUnit(unit: StudyUnit) =
        unitDao.deleteUnit(unit.toEntity())

    suspend fun countUnitsByCourse(courseId: Long): Int =
        unitDao.countUnitsByCourse(courseId)

    // ── Lesson ──
    fun getLessonsByUnit(unitId: Long): Flow<List<Lesson>> =
        lessonDao.getLessonsByUnit(unitId).map { list ->
            list.map { it.toModel() }
        }

    suspend fun getLessonById(id: Long): Lesson? =
        lessonDao.getLessonById(id)?.toModel()

    suspend fun insertLesson(lesson: Lesson) =
        lessonDao.insertLesson(lesson.toEntity())

    suspend fun updateLesson(lesson: Lesson) =
        lessonDao.updateLesson(lesson.toEntity())

    suspend fun deleteLesson(lesson: Lesson) =
        lessonDao.deleteLesson(lesson.toEntity())

    suspend fun countLessonsByUnit(unitId: Long): Int =
        lessonDao.countLessonsByUnit(unitId)

    suspend fun countLessonsByCourse(courseId: Long): Int =
        lessonDao.countLessonsByCourse(courseId)

    // ── Vocabulary ──
    fun getVocabularyByLesson(lessonId: Long): Flow<List<Vocabulary>> =
        vocabularyDao.getVocabularyByLesson(lessonId).map { list ->
            list.map { it.toModel() }
        }

    suspend fun getVocabularyById(id: Long): Vocabulary? =
        vocabularyDao.getVocabularyById(id)?.toModel()

    suspend fun countVocabularyByLesson(lessonId: Long): Int =
        vocabularyDao.countByLesson(lessonId)

    suspend fun countVocabularyByUnit(unitId: Long): Int =
        vocabularyDao.countByUnit(unitId)

    suspend fun countVocabularyByCourse(courseId: Long): Int =
        vocabularyDao.countByCourse(courseId)

    suspend fun insertVocabulary(vocab: Vocabulary) =
        vocabularyDao.insertVocabulary(vocab.toEntity())

    suspend fun insertVocabularyList(vocabs: List<Vocabulary>) =
        vocabularyDao.insertVocabularyList(vocabs.map { it.toEntity() })

    suspend fun updateVocabulary(vocab: Vocabulary) =
        vocabularyDao.updateVocabulary(vocab.toEntity())

    suspend fun deleteVocabulary(vocab: Vocabulary) =
        vocabularyDao.deleteVocabulary(vocab.toEntity())
}