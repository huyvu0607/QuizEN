// UserEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lumina.app.data.model.EnglishLevel

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String? = null,
    @ColumnInfo(name = "level") val level: String,        // lưu "A1".."C2"
    @ColumnInfo(name = "goal") val goal: String,
    @ColumnInfo(name = "streak_count") val streakCount: Int = 0,
    @ColumnInfo(name = "total_xp") val totalXp: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long
)