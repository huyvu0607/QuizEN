package com.lumina.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lumina.app.data.model.*
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreSync"
    }

    // ── User Sync ──
    suspend fun syncUser(user: User, firebaseUid: String) {
        val userMap = hashMapOf(
            "displayName" to user.displayName,
            "email" to user.email,
            "avatarUrl" to user.avatarUrl,
            "level" to user.level.name,
            "goal" to user.goal,
            "streakCount" to user.streakCount,
            "totalXp" to user.totalXp,
            "createdAt" to user.createdAt
        )
        db.collection("users").document(firebaseUid).set(userMap, SetOptions.merge()).await()
        Log.d(TAG, "Synced user info for $firebaseUid")
    }

    // ── Course Sync ──
    suspend fun syncCourse(firebaseUid: String, course: Course) {
        val courseMap = hashMapOf(
            "title" to course.title,
            "description" to course.description,
            "level" to course.level?.name,
            "coverColor" to course.coverColor,
            "coverIcon" to course.coverIcon,
            "isPublic" to course.isPublic,
            "createdAt" to course.createdAt
        )
        db.collection("users").document(firebaseUid)
            .collection("courses").document(course.id.toString())
            .set(courseMap, SetOptions.merge()).await()
        Log.d(TAG, "Synced course ${course.id}: ${course.title}")
    }

    suspend fun deleteCourse(firebaseUid: String, courseId: Long) {
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .delete().await()
        Log.d(TAG, "Deleted course $courseId")
    }

    suspend fun deleteUnit(firebaseUid: String, courseId: Long, unitId: Long) {
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .delete().await()
    }

    suspend fun deleteLesson(firebaseUid: String, courseId: Long, unitId: Long, lessonId: Long) {
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lessonId.toString())
            .delete().await()
    }

    // ── Unit Sync ──
    suspend fun syncUnit(firebaseUid: String, courseId: Long, unit: StudyUnit) {
        val unitMap = hashMapOf(
            "title" to unit.title,
            "icon" to unit.icon,
            "orderIndex" to unit.orderIndex,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .collection("units").document(unit.id.toString())
            .set(unitMap, SetOptions.merge()).await()
        Log.d(TAG, "Synced unit ${unit.id}: ${unit.title}")
    }

    // ── Lesson Sync ──
    suspend fun syncLesson(firebaseUid: String, courseId: Long, unitId: Long, lesson: Lesson) {
        val lessonMap = hashMapOf(
            "title" to lesson.title,
            "description" to lesson.description,
            "icon" to lesson.icon,
            "orderIndex" to lesson.orderIndex,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lesson.id.toString())
            .set(lessonMap, SetOptions.merge()).await()
        Log.d(TAG, "Synced lesson ${lesson.id}: ${lesson.title}")
    }

    // ── Vocabulary Sync ──
    suspend fun syncVocabulary(
        firebaseUid: String,
        courseId: Long,
        unitId: Long,
        lessonId: Long,
        vocab: Vocabulary
    ) {
        val vocabMap = hashMapOf(
            "word" to vocab.word,
            "meaning" to vocab.meaning,
            "ipa" to vocab.ipa,
            "wordType" to vocab.wordType?.name,
            "exampleSentence" to vocab.exampleSentence,
            "audioUrl" to vocab.audioUrl,
            "isFavorite" to vocab.isFavorite,
            "createdAt" to vocab.createdAt
        )
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lessonId.toString())
            .collection("vocabularies").document(vocab.id.toString())
            .set(vocabMap, SetOptions.merge()).await()
        Log.d(TAG, "Synced vocabulary ${vocab.id}: ${vocab.word}")
    }

    suspend fun deleteVocabulary(
        firebaseUid: String,
        courseId: Long,
        unitId: Long,
        lessonId: Long,
        vocabId: Long
    ) {
        db.collection("users").document(firebaseUid)
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lessonId.toString())
            .collection("vocabularies").document(vocabId.toString())
            .delete().await()
    }

    // ── Fetching Data (for syncing back to a new device) ──
    suspend fun fetchAllCourses(firebaseUid: String): List<Course> {
        return try {
            val snapshot = db.collection("users").document(firebaseUid)
                .collection("courses").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull() ?: return@mapNotNull null
                Course(
                    id = id,
                    userId = 0,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    level = try { EnglishLevel.valueOf(doc.getString("level") ?: "A1") } catch (e: Exception) { EnglishLevel.A1 },
                    coverColor = doc.getString("coverColor"),
                    coverIcon = doc.getString("coverIcon"),
                    isPublic = doc.getBoolean("isPublic") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllCourses failed: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchAllUnits(firebaseUid: String, courseId: Long): List<StudyUnit> {
        return try {
            val snapshot = db.collection("users").document(firebaseUid)
                .collection("courses").document(courseId.toString())
                .collection("units").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull() ?: return@mapNotNull null
                StudyUnit(
                    id = id,
                    courseId = courseId,
                    title = doc.getString("title") ?: "",
                    icon = doc.getString("icon"),
                    orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllUnits failed: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchAllLessons(firebaseUid: String, courseId: Long, unitId: Long): List<Lesson> {
        return try {
            val snapshot = db.collection("users").document(firebaseUid)
                .collection("courses").document(courseId.toString())
                .collection("units").document(unitId.toString())
                .collection("lessons").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull() ?: return@mapNotNull null
                Lesson(
                    id = id,
                    unitId = unitId,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    icon = doc.getString("icon"),
                    orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllLessons failed: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchAllVocabularies(
        firebaseUid: String,
        courseId: Long,
        unitId: Long,
        lessonId: Long
    ): List<Vocabulary> {
        return try {
            val snapshot = db.collection("users").document(firebaseUid)
                .collection("courses").document(courseId.toString())
                .collection("units").document(unitId.toString())
                .collection("lessons").document(lessonId.toString())
                .collection("vocabularies").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull() ?: return@mapNotNull null
                Vocabulary(
                    id = id,
                    lessonId = lessonId,
                    word = doc.getString("word") ?: "",
                    meaning = doc.getString("meaning") ?: "",
                    ipa = doc.getString("ipa"),
                    wordType = doc.getString("wordType")?.let { try { WordType.valueOf(it) } catch (e: Exception) { null } },
                    exampleSentence = doc.getString("exampleSentence"),
                    audioUrl = doc.getString("audioUrl"),
                    isFavorite = doc.getBoolean("isFavorite") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllVocabularies failed: ${e.message}", e)
            emptyList()
        }
    }
}
