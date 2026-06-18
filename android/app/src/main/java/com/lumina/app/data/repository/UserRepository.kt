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

    suspend fun insertUser(user: User) =
        userDao.insertUser(user.toEntity())

    suspend fun updateUser(user: User) =
        userDao.updateUser(user.toEntity())

    fun getBadgesByUser(userId: Long): Flow<List<UserBadge>> =
        userDao.getBadgesByUser(userId).map { list ->
            list.map { UserBadge(it.id, it.userId, it.badgeCode, it.earnedAt) }
        }

    suspend fun insertBadge(badge: UserBadge) =
        userDao.insertBadge(
            UserBadgeEntity(badge.id, badge.userId, badge.badgeCode, badge.earnedAt)
        )
}