// UnitDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.StudyUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {

    @Query("SELECT * FROM units WHERE course_id = :courseId ORDER BY order_index ASC")
    fun getUnitsByCourse(courseId: Long): Flow<List<StudyUnitEntity>>

    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitById(unitId: Long): StudyUnitEntity?

    @Query("SELECT COUNT(*) FROM units WHERE course_id = :courseId")
    suspend fun countUnitsByCourse(courseId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: StudyUnitEntity)

    @Update
    suspend fun updateUnit(unit: StudyUnitEntity)

    @Delete
    suspend fun deleteUnit(unit: StudyUnitEntity)
}