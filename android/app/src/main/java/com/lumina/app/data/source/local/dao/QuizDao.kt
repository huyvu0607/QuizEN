// QuizDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.QuizAnswerEntity
import com.lumina.app.data.source.local.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz_sessions WHERE user_id = :userId ORDER BY completed_at DESC")
    fun getSessionsByUser(userId: Long): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM quiz_answers WHERE session_id = :sessionId")
    suspend fun getAnswersBySession(sessionId: Long): List<QuizAnswerEntity>

    // Lấy từ yếu nhất — những từ hay sai nhất
    @Query("""
        SELECT vocabulary_id
        FROM quiz_answers 
        WHERE is_correct = 0 
        GROUP BY vocabulary_id 
        ORDER BY COUNT(*) DESC
        LIMIT :limit
    """)
    suspend fun getWeakestVocabularyIds(limit: Int = 20): List<Long>

    @Insert
    suspend fun insertSession(session: QuizSessionEntity): Long

    @Insert
    suspend fun insertAnswers(answers: List<QuizAnswerEntity>)
}