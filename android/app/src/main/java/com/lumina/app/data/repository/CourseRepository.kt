// CourseRepository.kt
package com.lumina.app.data.repository

import com.lumina.app.data.model.*
import com.lumina.app.data.source.local.dao.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepository(
    private val courseDao: CourseDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val vocabularyDao: VocabularyDao
) {
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