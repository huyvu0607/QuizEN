// UnitEntity.kt
package com.lumina.app.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "units",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["course_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class StudyUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "course_id", index = true) val courseId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int
)