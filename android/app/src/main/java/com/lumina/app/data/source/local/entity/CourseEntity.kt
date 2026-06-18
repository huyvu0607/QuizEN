// CourseEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CourseEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "user_id", index = true) val userId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "is_public") val isPublic: Boolean = false,
    @ColumnInfo(name = "cover_color") val coverColor: String? = null,
    @ColumnInfo(name = "cover_icon") val coverIcon: String? = null,
    @ColumnInfo(name = "level") val level: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)