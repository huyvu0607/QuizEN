// Mapper.kt
package com.lumina.app.data.model

import com.lumina.app.data.source.local.entity.*

// ─── USER ───────────────────────────────────────────
fun UserEntity.toModel() = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    level = EnglishLevel.valueOf(level),
    goal = goal,
    streakCount = streakCount,
    totalXp = totalXp,
    createdAt = createdAt
)

fun User.toEntity() = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    level = level.name,
    goal = goal,
    streakCount = streakCount,
    totalXp = totalXp,
    createdAt = createdAt
)

// ─── COURSE ─────────────────────────────────────────
fun CourseEntity.toModel() = Course(
    id = id,
    userId = userId,
    title = title,
    description = description,
    isPublic = isPublic,
    coverColor = coverColor,
    coverIcon = coverIcon,
    level = level?.let { EnglishLevel.valueOf(it) },
    createdAt = createdAt
)

fun Course.toEntity() = CourseEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    isPublic = isPublic,
    coverColor = coverColor,
    coverIcon = coverIcon,
    level = level?.name,
    createdAt = createdAt
)

// ─── UNIT ────────────────────────────────────────────
fun StudyUnitEntity.toModel() = StudyUnit(
    id = id,
    courseId = courseId,
    title = title,
    orderIndex = orderIndex
)

fun StudyUnit.toEntity() = StudyUnitEntity(
    id = id,
    courseId = courseId,
    title = title,
    orderIndex = orderIndex
)

// ─── LESSON ──────────────────────────────────────────
fun LessonEntity.toModel() = Lesson(
    id = id,
    unitId = unitId,
    title = title,
    description = description,
    orderIndex = orderIndex
)

fun Lesson.toEntity() = LessonEntity(
    id = id,
    unitId = unitId,
    title = title,
    description = description,
    orderIndex = orderIndex
)

// ─── VOCABULARY ──────────────────────────────────────
fun VocabularyEntity.toModel() = Vocabulary(
    id = id,
    lessonId = lessonId,
    word = word,
    meaning = meaning,
    exampleSentence = exampleSentence,
    ipa = ipa,
    wordType = wordType?.let { WordType.valueOf(it) },
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    isFavorite = isFavorite,
    createdAt = createdAt
)

fun Vocabulary.toEntity() = VocabularyEntity(
    id = id,
    lessonId = lessonId,
    word = word,
    meaning = meaning,
    exampleSentence = exampleSentence,
    ipa = ipa,
    wordType = wordType?.name,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    isFavorite = isFavorite,
    createdAt = createdAt
)

// ─── TOPIC GROUP ─────────────────────────────────────
fun TopicGroupEntity.toModel() = TopicGroup(
    id = id,
    unitId = unitId,
    name = name,
    isAiGenerated = isAiGenerated,
    orderIndex = orderIndex,
    createdAt = createdAt
)

fun TopicGroup.toEntity() = TopicGroupEntity(
    id = id,
    unitId = unitId,
    name = name,
    isAiGenerated = isAiGenerated,
    orderIndex = orderIndex,
    createdAt = createdAt
)

// ─── SRS CARD ────────────────────────────────────────
fun SrsCardEntity.toModel() = SrsCard(
    id = id,
    userId = userId,
    vocabularyId = vocabularyId,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    repetition = repetition,
    nextReviewAt = nextReviewAt,
    lastReviewedAt = lastReviewedAt
)

fun SrsCard.toEntity() = SrsCardEntity(
    id = id,
    userId = userId,
    vocabularyId = vocabularyId,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    repetition = repetition,
    nextReviewAt = nextReviewAt,
    lastReviewedAt = lastReviewedAt
)

// ─── QUIZ ────────────────────────────────────────────
fun QuizSessionEntity.toModel() = QuizSession(
    id = id,
    userId = userId,
    scopeType = QuizScopeType.valueOf(scopeType),
    scopeId = scopeId,
    totalQuestions = totalQuestions,
    correctCount = correctCount,
    score = score,
    completedAt = completedAt
)

fun QuizSession.toEntity() = QuizSessionEntity(
    id = id,
    userId = userId,
    scopeType = scopeType.name,
    scopeId = scopeId,
    totalQuestions = totalQuestions,
    correctCount = correctCount,
    score = score,
    completedAt = completedAt
)

fun QuizAnswerEntity.toModel() = QuizAnswer(
    id = id,
    sessionId = sessionId,
    vocabularyId = vocabularyId,
    questionType = QuizQuestionType.valueOf(questionType),
    userAnswer = userAnswer,
    isCorrect = isCorrect,
    timeTakenMs = timeTakenMs
)

fun QuizAnswer.toEntity() = QuizAnswerEntity(
    id = id,
    sessionId = sessionId,
    vocabularyId = vocabularyId,
    questionType = questionType.name,
    userAnswer = userAnswer,
    isCorrect = isCorrect,
    timeTakenMs = timeTakenMs
)