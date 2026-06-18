// AppDatabase.kt
package com.lumina.app.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lumina.app.data.source.local.dao.*
import com.lumina.app.data.source.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        UserBadgeEntity::class,
        CourseEntity::class,
        StudyUnitEntity::class,
        LessonEntity::class,
        VocabularyEntity::class,
        TopicGroupEntity::class,
        TopicGroupWordEntity::class,
        SrsCardEntity::class,
        QuizSessionEntity::class,
        QuizAnswerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDao(): LessonDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun topicGroupDao(): TopicGroupDao
    abstract fun srsDao(): SrsDao
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocabquiz_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}