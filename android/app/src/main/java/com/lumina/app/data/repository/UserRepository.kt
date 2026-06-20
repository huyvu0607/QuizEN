// UserRepository.kt
package com.lumina.app.data.repository

import com.lumina.app.data.model.*
import com.lumina.app.data.source.local.dao.UserDao
import com.lumina.app.data.source.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserById(userId: Long): User? =
        userDao.getUserById(userId)?.toModel()

    suspend fun getUserByEmail(email: String): User? =
        userDao.getUserByEmail(email)?.toModel()

    suspend fun insertUser(user: User) =
        userDao.insertUser(user.toEntity())

    suspend fun updateUser(user: User) =
        userDao.updateUser(user.toEntity())

    /**
     * Lưu/cập nhật User local (Room) ngay sau khi đăng nhập/đăng ký thành công qua Firebase Auth.
     * Nếu email đã tồn tại trong Room thì cập nhật, chưa có thì tạo mới.
     */
    suspend fun upsertFromAuth(
        email: String,
        displayName: String,
        avatarUrl: String?,
        level: EnglishLevel = EnglishLevel.A1,
        goal: String = "Giao tiếp hàng ngày"
    ): User {
        val existing = getUserByEmail(email)
        if (existing != null) {
            val updated = existing.copy(
                displayName = displayName,
                avatarUrl = avatarUrl ?: existing.avatarUrl
            )
            updateUser(updated)
            return updated
        }

        val newUser = User(
            id = generateUserId(),
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            level = level,
            goal = goal,
            streakCount = 0,
            totalXp = 0,
            createdAt = System.currentTimeMillis()
        )
        insertUser(newUser)
        return newUser
    }

    /**
     * UserEntity.id không dùng autoGenerate, nên ta tự sinh ID duy nhất.
     * Dùng timestamp millis làm id là đủ an toàn cho ngữ cảnh 1 user đăng ký tại 1 thời điểm.
     */
    private fun generateUserId(): Long = System.currentTimeMillis()

    fun getBadgesByUser(userId: Long): Flow<List<UserBadge>> =
        userDao.getBadgesByUser(userId).map { list ->
            list.map { UserBadge(it.id, it.userId, it.badgeCode, it.earnedAt) }
        }

    suspend fun insertBadge(badge: UserBadge) =
        userDao.insertBadge(
            UserBadgeEntity(badge.id, badge.userId, badge.badgeCode, badge.earnedAt)
        )
}