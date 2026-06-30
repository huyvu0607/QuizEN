package com.lumina.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lumina.app.data.model.*
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager {
    private val db = FirebaseFirestore.getInstance()

    // ── Course Sync ──
    suspend fun syncCourse(userId: Long, course: Course) {
        val courseMap = hashMapOf(
            "title" to course.title,
            "description" to course.description,
            "level" to course.level?.name,
            "coverColor" to course.coverColor,
            "coverIcon" to course.coverIcon,
            "isPublic" to course.isPublic,
            "createdAt" to course.createdAt
        )
        db.collection("users").document(userId.toString())
            .collection("courses").document(course.id.toString())
            .set(courseMap, SetOptions.merge()).await()
    }

    suspend fun deleteCourse(userId: Long, courseId: Long) {
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .delete().await()
    }

    suspend fun deleteUnit(userId: Long, courseId: Long, unitId: Long) {
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .delete().await()
    }

    suspend fun deleteLesson(userId: Long, courseId: Long, unitId: Long, lessonId: Long) {
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lessonId.toString())
            .delete().await()
    }

    // ── Unit Sync ──
    suspend fun syncUnit(userId: Long, courseId: Long, unit: StudyUnit) {
        val unitMap = hashMapOf(
            "title" to unit.title,
            "orderIndex" to unit.orderIndex
        )
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .collection("units").document(unit.id.toString())
            .set(unitMap, SetOptions.merge()).await()
    }

    // ── Lesson Sync ──
    suspend fun syncLesson(userId: Long, courseId: Long, unitId: Long, lesson: Lesson) {
        val lessonMap = hashMapOf(
            "title" to lesson.title,
            "description" to lesson.description,
            "orderIndex" to lesson.orderIndex
        )
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lesson.id.toString())
            .set(lessonMap, SetOptions.merge()).await()
    }

    // ── Vocabulary Sync ──
    suspend fun syncVocabulary(userId: Long, courseId: Long, unitId: Long, lessonId: Long, vocab: Vocabulary) {
        val vocabMap = hashMapOf(
            "word" to vocab.word,
            "meaning" to vocab.meaning,
            "ipa" to vocab.ipa,
            "wordType" to vocab.wordType?.name,
            "exampleSentence" to vocab.exampleSentence,
            "audioUrl" to vocab.audioUrl,
            "createdAt" to vocab.createdAt
        )
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lessonId.toString())
            .collection("vocabularies").document(vocab.id.toString())
            .set(vocabMap, SetOptions.merge()).await()
    }
    
    suspend fun deleteVocabulary(userId: Long, courseId: Long, unitId: Long, lessonId: Long, vocabId: Long) {
        db.collection("users").document(userId.toString())
            .collection("courses").document(courseId.toString())
            .collection("units").document(unitId.toString())
            .collection("lessons").document(lessonId.toString())
            .collection("vocabularies").document(vocabId.toString())
            .delete().await()
    }

    // ── Fetching Data (for syncing back to a new device) ──
    suspend fun fetchAllCourses(userId: Long): List<Course> {
        return try {
            val snapshot = db.collection("users").document(userId.toString())
                .collection("courses").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull() ?: return@mapNotNull null
                Course(
                    id = id,
                    userId = userId,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    level = try { EnglishLevel.valueOf(doc.getString("level") ?: "A1") } catch(e: Exception) { EnglishLevel.A1 },
                    coverColor = doc.getString("coverColor"),
                    coverIcon = doc.getString("coverIcon"),
                    isPublic = doc.getBoolean("isPublic") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAllUnits(userId: Long, courseId: Long): List<StudyUnit> {
        return try {
            val snapshot = db.collection("users").document(userId.toString())
                .collection("courses").document(courseId.toString())
                .collection("units").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull() ?: return@mapNotNull null
                StudyUnit(
                    id = id,
                    courseId = courseId,
                    title = doc.getString("title") ?: "",
                    orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun fetchAllLessons(userId: Long, courseId: Long, unitId: Long): List<Lesson> {
        return try {
            val snapshot = db.collection("users").document(userId.toString())
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
                    orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun fetchAllVocabularies(userId: Long, courseId: Long, unitId: Long, lessonId: Long): List<Vocabulary> {
        return try {
            val snapshot = db.collection("users").document(userId.toString())
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
                    wordType = doc.getString("wordType")?.let { try { WordType.valueOf(it) } catch(e: Exception) { null } },
                    exampleSentence = doc.getString("exampleSentence"),
                    audioUrl = doc.getString("audioUrl"),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}
