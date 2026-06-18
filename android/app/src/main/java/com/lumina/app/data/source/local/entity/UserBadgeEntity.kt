// UserBadgeEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_badges",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserBadgeEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "user_id", index = true) val userId: Long,
    @ColumnInfo(name = "badge_code") val badgeCode: String,
    @ColumnInfo(name = "earned_at") val earnedAt: Long
)