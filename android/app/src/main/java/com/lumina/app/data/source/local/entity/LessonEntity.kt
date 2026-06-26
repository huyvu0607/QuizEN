// LessonEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(
        entity = StudyUnitEntity::class,
        parentColumns = ["id"],
        childColumns = ["unit_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "unit_id", index = true) val unitId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "order_index") val orderIndex: Int
)