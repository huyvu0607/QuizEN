// VocabularyDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabulary WHERE lesson_id = :lessonId")
    fun getVocabularyByLesson(lessonId: Long): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE id = :vocabId")
    suspend fun getVocabularyById(vocabId: Long): VocabularyEntity?

    // Đếm số từ trong lesson — thay thế cho wordCount trong Entity
    @Query("SELECT COUNT(*) FROM vocabulary WHERE lesson_id = :lessonId")
    suspend fun countByLesson(lessonId: Long): Int

    // Lấy từ theo nhiều lesson (quiz trộn)
    @Query("SELECT * FROM vocabulary WHERE lesson_id IN (:lessonIds)")
    suspend fun getVocabularyByLessons(lessonIds: List<Long>): List<VocabularyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocab: VocabularyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabularyList(vocabs: List<VocabularyEntity>)

    @Update
    suspend fun updateVocabulary(vocab: VocabularyEntity)

    @Delete
    suspend fun deleteVocabulary(vocab: VocabularyEntity)
}