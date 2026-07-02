package com.lumina.app.data.repository

import com.lumina.app.data.source.local.dao.*
import com.lumina.app.data.model.*
import com.lumina.app.ui.home.ContinueLearningState
import com.lumina.app.ui.home.CourseCardState
import com.lumina.app.ui.home.HomeUiState
import com.lumina.app.ui.home.SyncStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class HomeRepository(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val vocabularyDao: VocabularyDao,
    private val srsDao: SrsDao,
    private val quizDao: QuizDao
) {

    private val _syncState = MutableStateFlow<Pair<SyncStatus, Int>>(SyncStatus.IDLE to 0)
    val syncState: StateFlow<Pair<SyncStatus, Int>> = _syncState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHomeData(userId: Long): Flow<HomeUiState> {
        val now = System.currentTimeMillis()
        return combine(
            userDao.getUserByIdFlow(userId),
            courseDao.getCoursesByUser(userId),
            srsDao.countDueCards(userId, now),
            syncState
        ) { user, courses, dueCount, sync ->
            val (status, progress) = sync
            DataBundle(user, courses, dueCount, status, progress)
        }.flatMapLatest { bundle ->
            flow {
                val (user, courses, dueCount, status, progress) = bundle
                val courseStates = courses.take(2).map { course ->
                    val totalWords = vocabularyDao.countByCourse(course.id)
                    val learnedWords = vocabularyDao.countLearnedByCourse(course.id)
                    CourseCardState(
                        id = course.id.toString(),
                        name = course.title,
                        wordsLearned = learnedWords,
                        wordsTotal = totalWords,
                        iconRes = getIconRes(course.coverIcon),
                        iconBgRes = com.lumina.app.R.drawable.bg_icon_square_gray,
                        coverColor = course.coverColor,
                        progressBgRes = com.lumina.app.R.drawable.bg_progress_fill_primary
                    )
                }

                val totalLearnedGlobal = vocabularyDao.countTotalLearned()
                val totalCorrect = quizDao.getTotalCorrectAnswers(userId) ?: 0
                val totalQuestions = quizDao.getTotalQuestionsAsked(userId) ?: 0
                val accuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0
                
                emit(HomeUiState(
                    userName = user?.displayName ?: "Người dùng",
                    streakDays = user?.streakCount ?: 0,
                    totalWords = totalLearnedGlobal,
                    accuracyPercent = accuracy,
                    currentXp = user?.totalXp ?: 0,
                    goalXp = user?.goal?.toIntOrNull() ?: 500,
                    continueLearning = courses.firstOrNull()?.let {
                        val total = vocabularyDao.countByCourse(it.id)
                        val learned = vocabularyDao.countLearnedByCourse(it.id)
                        val progressVal = if (total > 0) (learned * 100) / total else 0
                        ContinueLearningState(
                            courseTitle = it.title,
                            level = it.level ?: "N/A",
                            progressPercent = progressVal,
                            iconRes = getIconRes(it.coverIcon),
                            coverColor = it.coverColor
                        )
                    },
                    reviewWordsCount = dueCount,
                    courses = courseStates,
                    syncStatus = status,
                    syncProgress = progress
                ))
            }
        }
    }

    private data class DataBundle(
        val user: com.lumina.app.data.source.local.entity.UserEntity?,
        val courses: List<com.lumina.app.data.source.local.entity.CourseEntity>,
        val dueCount: Int,
        val status: SyncStatus,
        val progress: Int
    )

    suspend fun syncFromCloud(userId: Long, firebaseUid: String) {
        if (_syncState.value.first == SyncStatus.SYNCING) return
        
        val firestoreSync = FirestoreSyncManager()
        try {
            android.util.Log.d("HomeRepository", "Bắt đầu đồng bộ sâu...")
            _syncState.value = SyncStatus.SYNCING to 0
            val remoteCourses = firestoreSync.fetchAllCourses(firebaseUid)
            
            if (remoteCourses.isEmpty()) {
                _syncState.value = SyncStatus.COMPLETED to 100
                return
            }

            remoteCourses.forEachIndexed { index, remoteCourse ->
                if (_syncState.value.first == SyncStatus.PAUSED) return@forEachIndexed

                // 1. Lưu Khóa học
                courseDao.insertCourse(remoteCourse.copy(userId = userId).toEntity())
                
                // 2. Tải và lưu Unit
                val remoteUnits = firestoreSync.fetchAllUnits(firebaseUid, remoteCourse.id)
                remoteUnits.forEach { remoteUnit ->
                    unitDao.insertUnit(remoteUnit.toEntity())
                    
                    // 3. Tải và lưu Lesson
                    val remoteLessons = firestoreSync.fetchAllLessons(firebaseUid, remoteCourse.id, remoteUnit.id)
                    remoteLessons.forEach { remoteLesson ->
                        lessonDao.insertLesson(remoteLesson.toEntity())
                        
                        // 4. Tải và lưu Từ vựng (Quan trọng nhất)
                        val remoteVocabs = firestoreSync.fetchAllVocabularies(
                            firebaseUid, remoteCourse.id, remoteUnit.id, remoteLesson.id
                        )
                        if (remoteVocabs.isNotEmpty()) {
                            vocabularyDao.insertVocabularyList(remoteVocabs.map { it.toEntity() })
                        }
                    }
                }
                
                val currentProgress = ((index + 1) * 100) / remoteCourses.size
                _syncState.value = SyncStatus.SYNCING to currentProgress
            }
            _syncState.value = SyncStatus.COMPLETED to 100
            android.util.Log.d("HomeRepository", "Đồng bộ sâu hoàn tất!")
        } catch (e: Exception) {
            android.util.Log.e("HomeRepository", "Sync failed: ${e.message}")
            _syncState.value = SyncStatus.ERROR to 0
        }
    }

    fun pauseSync() {
        _syncState.value = SyncStatus.PAUSED to _syncState.value.second
    }

    fun resumeSync() {
        if (_syncState.value.first == SyncStatus.PAUSED) {
            _syncState.value = SyncStatus.IDLE to _syncState.value.second
        }
    }

    private fun getIconRes(iconKey: String?): Int {
        return when (iconKey) {
            "book" -> com.lumina.app.R.drawable.ic_book
            "star" -> com.lumina.app.R.drawable.ic_star
            "trophy" -> com.lumina.app.R.drawable.ic_trophy
            "brain" -> com.lumina.app.R.drawable.ic_brain
            "globe" -> com.lumina.app.R.drawable.ic_globe
            "briefcase" -> com.lumina.app.R.drawable.ic_briefcase
            "plane" -> com.lumina.app.R.drawable.ic_plane
            else -> com.lumina.app.R.drawable.ic_book
        }
    }
}
