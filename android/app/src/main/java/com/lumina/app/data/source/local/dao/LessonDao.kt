// LessonDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE unit_id = :unitId ORDER BY order_index ASC")
    fun getLessonsByUnit(unitId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: Long): LessonEntity?

    @Query("SELECT COUNT(*) FROM lessons WHERE unit_id = :unitId")
    suspend fun countLessonsByUnit(unitId: Long): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE unit_id IN (SELECT id FROM units WHERE course_id = :courseId)")
    suspend fun countLessonsByCourse(courseId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)
}