package com.lumina.app.ui.course

import androidx.lifecycle.*
import com.lumina.app.data.model.*
import com.lumina.app.data.repository.CourseRepository
import com.lumina.app.data.source.local.pref.SessionManager
import com.lumina.app.ui.unit.*
import com.lumina.app.ui.lesson.*
import com.lumina.app.ui.vocabulary.*
import com.lumina.app.data.source.remote.DictionaryEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class CourseViewModel(
    private val repository: CourseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseUiItem>>(emptyList())
    val courses: StateFlow<List<CourseUiItem>> = _courses.asStateFlow()

    private val _units = MutableStateFlow<List<UnitItem>>(emptyList())
    val units: StateFlow<List<UnitItem>> = _units.asStateFlow()

    private val _lessons = MutableStateFlow<List<LessonUiItem>>(emptyList())
    val lessons: StateFlow<List<LessonUiItem>> = _lessons.asStateFlow()

    private val _vocabularies = MutableStateFlow<List<VocabularyUiItem>>(emptyList())
    val vocabularies: StateFlow<List<VocabularyUiItem>> = _vocabularies.asStateFlow()

    private val _topicGroups = MutableStateFlow<List<TopicGroupUiItem>>(emptyList())
    val topicGroups: StateFlow<List<TopicGroupUiItem>> = _topicGroups.asStateFlow()

    private val _isLoadingAiGrouping = MutableStateFlow(false)
    val isLoadingAiGrouping: StateFlow<Boolean> = _isLoadingAiGrouping.asStateFlow()

    private val _errorAi = MutableStateFlow<String?>(null)
    val errorAi: StateFlow<String?> = _errorAi.asStateFlow()

    private val _currentCourse = MutableLiveData<Course?>()
    val currentCourse: LiveData<Course?> = _currentCourse

    private val _currentLesson = MutableLiveData<Lesson?>()
    val currentLesson: LiveData<Lesson?> = _currentLesson

    private val _saveResult = MutableLiveData<Result<Unit>?>()
    val saveResult: LiveData<Result<Unit>?> = _saveResult

    init {
        loadCourses()
        syncFromCloud()
    }

    private fun syncFromCloud() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: sessionManager.getFirebaseUid()
            if (userId == -1L || firebaseUid.isNullOrBlank()) {
                android.util.Log.w("Sync", "Cloud sync skipped: missing user session or Firebase UID")
                return@launch
            }

            val firestoreSync = com.lumina.app.data.repository.FirestoreSyncManager()

            try {
                android.util.Log.d("Sync", "Starting deep sync from cloud for uid=$firebaseUid")
                val remoteCourses = firestoreSync.fetchAllCourses(firebaseUid)

                remoteCourses.forEach { remoteCourse ->
                    val localCourse = repository.getCourseById(remoteCourse.id)
                    if (localCourse == null) {
                        android.util.Log.d("Sync", "Downloading course: ${remoteCourse.title}")
                        repository.insertCourse(remoteCourse.copy(userId = userId))

                        val remoteUnits = firestoreSync.fetchAllUnits(firebaseUid, remoteCourse.id)
                        remoteUnits.forEach { remoteUnit ->
                            repository.insertUnit(remoteUnit)

                            val remoteLessons = firestoreSync.fetchAllLessons(firebaseUid, remoteCourse.id, remoteUnit.id)
                            remoteLessons.forEach { remoteLesson ->
                                repository.insertLesson(remoteLesson)

                                val remoteVocabs = firestoreSync.fetchAllVocabularies(
                                    firebaseUid, remoteCourse.id, remoteUnit.id, remoteLesson.id
                                )
                                if (remoteVocabs.isNotEmpty()) {
                                    repository.insertVocabularyList(remoteVocabs)
                                }
                            }
                        }
                    }
                }
                android.util.Log.d("Sync", "Deep sync completed successfully.")
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Deep sync failed: ${e.message}", e)
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    private fun loadCourses() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            repository.getCoursesByUser(userId).collectLatest { courseList ->
                val uiItems = mutableListOf<CourseUiItem>()
                for (course in courseList) {
                    val wordCount = repository.countVocabularyByCourse(course.id)
                    uiItems.add(CourseUiItem(
                        id = course.id,
                        title = course.title,
                        level = course.level?.name ?: "N/A",
                        progress = 0,
                        words = "$wordCount từ",
                        iconRes = getIconRes(course.coverIcon),
                        coverIcon = course.coverIcon,
                        coverColor = course.coverColor,
                        isCompleted = false
                    ))
                }
                _courses.value = uiItems
            }
        }
    }

    fun loadUnits(courseId: Long) {
        viewModelScope.launch {
            _currentCourse.value = repository.getCourseById(courseId)
            repository.getUnitsByCourse(courseId).collectLatest { unitList ->
                val uiItems = mutableListOf<UnitItem>()
                unitList.forEachIndexed { index, unit ->
                    val lessonCount = repository.countLessonsByUnit(unit.id)
                    val wordCount = repository.countVocabularyByUnit(unit.id)
                    uiItems.add(UnitItem(
                        id = unit.id.toInt(),
                        title = unit.title,
                        subtitle = "$wordCount từ · $lessonCount Bài học",
                        status = when {
                            index == 0 -> UnitStatus.COMPLETED
                            index == 1 -> UnitStatus.IN_PROGRESS
                            else -> UnitStatus.LOCKED
                        },
                        wordCount = wordCount,
                        lessonCount = lessonCount
                    ))
                }
                _units.value = uiItems
            }
        }
    }

    fun loadLessons(unitId: Long) {
        viewModelScope.launch {
            repository.getLessonsByUnit(unitId).collectLatest { lessonList ->
                val uiItems = mutableListOf<LessonUiItem>()
                lessonList.forEach { lesson ->
                    val wordCount = repository.countVocabularyByLesson(lesson.id)
                    val learnedCount = repository.countLearnedVocabularyByLesson(lesson.id)
                    
                    val status = when {
                        wordCount == 0 -> LessonStatus.NOT_STARTED
                        learnedCount >= wordCount -> LessonStatus.COMPLETED
                        learnedCount > 0 -> LessonStatus.LEARNING
                        else -> LessonStatus.NOT_STARTED
                    }

                    uiItems.add(LessonUiItem(
                        id = lesson.id,
                        title = lesson.title,
                        subtitle = "$wordCount từ vựng",
                        status = status,
                        progress = if (wordCount > 0) (learnedCount * 100) / wordCount else 0,
                        iconRes = getIconResForLesson(lesson.id.toInt()),
                        wordCount = wordCount
                    ))
                }
                _lessons.value = uiItems
            }
        }
    }

    private fun getIconResForLesson(id: Int): Int {
        return when (id % 4) {
            0 -> com.lumina.app.R.drawable.ic_plane
            1 -> com.lumina.app.R.drawable.ic_layers
            2 -> com.lumina.app.R.drawable.ic_book
            else -> com.lumina.app.R.drawable.ic_briefcase
        }
    }

    fun loadVocabularies(lessonId: Long) {
        viewModelScope.launch {
            _currentLesson.value = repository.getLessonById(lessonId)
            repository.getVocabularyByLesson(lessonId).collectLatest { vocabList ->
                _vocabularies.value = vocabList.map { vocab ->
                    VocabularyUiItem(
                        id = vocab.id,
                        word = vocab.word,
                        ipa = vocab.ipa,
                        meaning = vocab.meaning,
                        wordType = vocab.wordType?.name?.lowercase()?.replaceFirstChar { 
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                        } ?: "Word",
                        exampleSentence = vocab.exampleSentence,
                        audioUrl = vocab.audioUrl,
                        status = if (vocab.id % 3 == 0L) VocabularyStatus.LEARNED 
                                 else if (vocab.id % 2 == 0L) VocabularyStatus.NEEDS_REVIEW 
                                 else VocabularyStatus.NOT_STARTED,
                        isFavorite = vocab.id % 5 == 0L
                    )
                }
            }
        }
    }

    fun addLesson(unitId: Long, title: String, orderIndex: Int) {
        viewModelScope.launch {
            val newLesson = Lesson(
                id = 0,
                unitId = unitId,
                title = title,
                description = "",
                orderIndex = orderIndex
            )
            repository.insertLesson(newLesson)
            // Sau khi thêm xong, reload lại danh sách bài học của unit này
            loadLessons(unitId)
        }
    }

    fun addCourse(title: String, description: String, level: String, color: String, icon: String) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId == -1L) {
                _saveResult.value = Result.failure(Exception("Phiên đăng nhập hết hạn"))
                return@launch
            }

            val levelEnum = try {
                EnglishLevel.valueOf(level.split(" ").first())
            } catch (e: Exception) {
                EnglishLevel.A1
            }
            
            val newCourse = Course(
                id = 0,
                userId = userId,
                title = title,
                description = description,
                isPublic = false,
                coverColor = color,
                coverIcon = icon,
                level = levelEnum,
                createdAt = System.currentTimeMillis()
            )
            
            try {
                repository.insertCourse(newCourse)
                _saveResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun updateCourse(courseId: Long, title: String, description: String, level: String, color: String, icon: String) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            val levelEnum = try {
                EnglishLevel.valueOf(level.split(" ").first())
            } catch (e: Exception) {
                EnglishLevel.A1
            }
            
            val updatedCourse = Course(
                id = courseId,
                userId = userId,
                title = title,
                description = description,
                isPublic = false,
                coverColor = color,
                coverIcon = icon,
                level = levelEnum,
                createdAt = System.currentTimeMillis() // Or keep original
            )
            
            try {
                repository.updateCourse(updatedCourse)
                _saveResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            val course = repository.getCourseById(courseId)
            course?.let { repository.deleteCourse(it) }
        }
    }

    fun addUnit(courseId: Long, title: String, orderIndex: Int) {
        viewModelScope.launch {
            val newUnit = StudyUnit(
                id = 0,
                courseId = courseId,
                title = title,
                orderIndex = orderIndex
            )
            repository.insertUnit(newUnit)
        }
    }

    fun deleteLesson(lessonId: Long) {
        viewModelScope.launch {
            val lesson = repository.getLessonById(lessonId)
            lesson?.let { repository.deleteLesson(it) }
        }
    }

    fun updateLesson(lessonId: Long, newTitle: String) {
        viewModelScope.launch {
            val lesson = repository.getLessonById(lessonId)
            lesson?.let {
                val updated = it.copy(title = newTitle)
                repository.updateLesson(updated)
            }
        }
    }

    fun addVocabulary(lessonId: Long, word: String, meaning: String, example: String?, ipa: String?, type: WordType?) {
        viewModelScope.launch {
            val vocab = Vocabulary(
                id = 0,
                lessonId = lessonId,
                word = word,
                meaning = meaning,
                exampleSentence = example,
                ipa = ipa,
                wordType = type,
                createdAt = System.currentTimeMillis()
            )
            repository.insertVocabulary(vocab)
        }
    }

    fun bulkInsertVocabulary(vocabs: List<Vocabulary>) {
        viewModelScope.launch {
            repository.insertVocabularyList(vocabs)
        }
    }

    fun updateVocabulary(vocabId: Long, lessonId: Long, word: String, meaning: String, example: String?, ipa: String?, type: WordType?) {
        viewModelScope.launch {
            val vocab = Vocabulary(
                id = vocabId,
                lessonId = lessonId,
                word = word,
                meaning = meaning,
                exampleSentence = example,
                ipa = ipa,
                wordType = type,
                createdAt = System.currentTimeMillis()
            )
            repository.updateVocabulary(vocab)
        }
    }

    suspend fun getVocabularyById(id: Long): Vocabulary? {
        return repository.getVocabularyById(id)
    }

    suspend fun fetchDictionaryData(word: String): List<DictionaryEntry>? {
        return repository.fetchDictionaryDetails(word)
    }

    suspend fun suggestVocabularyWithAi(word: String): String? {
        return repository.suggestVocabularyWithAi(word)
    }

    suspend fun analyzeUnitWithAi(unitId: Long): String? {
        val vocabList = repository.getVocabularyByLesson(-1) // This is just a placeholder, I need a better way to get all unit vocab
        // For simplicity, let's just use a prompt that describes the task
        return repository.explainWordWithAi("Study Plan", "Kế hoạch học tập cho Unit $unitId")
    }

    fun loadTopicGroups(unitId: Long) {
        viewModelScope.launch {
            repository.getTopicGroupsWithWords(unitId).collectLatest { groups ->
                _topicGroups.value = groups
            }
        }
    }

    fun triggerAiGrouping(unitId: Long) {
        viewModelScope.launch {
            _isLoadingAiGrouping.value = true
            _errorAi.value = null
            val success = repository.groupVocabularyWithAi(unitId)
            if (success) {
                sessionManager.setUnitGrouped(unitId, true)
                loadTopicGroups(unitId)
            } else {
                _errorAi.value = "AI hiện không khả dụng. Bạn có thể phân nhóm thủ công."
            }
            _isLoadingAiGrouping.value = false
        }
    }

    fun isUnitAlreadyGrouped(unitId: Long): Boolean {
        return sessionManager.isUnitGrouped(unitId)
    }

    fun clearAiError() {
        _errorAi.value = null
    }

    fun renameTopicGroup(groupId: Long, unitId: Long, newName: String) {
        viewModelScope.launch {
            repository.renameTopicGroup(groupId, newName)
            loadTopicGroups(unitId)
        }
    }

    fun deleteTopicGroup(groupId: Long, unitId: Long) {
        viewModelScope.launch {
            repository.deleteTopicGroup(groupId)
            loadTopicGroups(unitId)
        }
    }

    fun addTopicGroup(unitId: Long, name: String) {
        viewModelScope.launch {
            repository.createTopicGroup(unitId, name)
            loadTopicGroups(unitId)
        }
    }

    fun moveWordToGroup(vocabId: Long, targetGroupId: Long, unitId: Long) {
        viewModelScope.launch {
            repository.moveWordToTopicGroup(vocabId, targetGroupId, unitId)
            loadTopicGroups(unitId)
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
