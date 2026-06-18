// SrsDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.SrsCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SrsDao {

    // Lấy từ cần ôn hôm nay
    @Query("""
        SELECT * FROM srs_records 
        WHERE user_id = :userId 
        AND next_review_at <= :now
        ORDER BY next_review_at ASC
    """)
    fun getDueCards(userId: Long, now: Long): Flow<List<SrsCardEntity>>

    @Query("SELECT COUNT(*) FROM srs_records WHERE user_id = :userId AND next_review_at <= :now")
    fun countDueCards(userId: Long, now: Long): Flow<Int>

    @Query("SELECT * FROM srs_records WHERE user_id = :userId AND vocabulary_id = :vocabId")
    suspend fun getCard(userId: Long, vocabId: Long): SrsCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SrsCardEntity)

    @Update
    suspend fun updateCard(card: SrsCardEntity)
}