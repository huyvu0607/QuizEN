// UserDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.UserEntity
import com.lumina.app.data.source.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    // Badges
    @Query("SELECT * FROM user_badges WHERE user_id = :userId")
    fun getBadgesByUser(userId: Long): Flow<List<UserBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: UserBadgeEntity)
}