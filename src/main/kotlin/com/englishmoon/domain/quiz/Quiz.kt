package com.englishmoon.domain.quiz

import java.time.OffsetDateTime
import java.util.UUID

data class Quiz(
    val id: UUID,
    val lessonId: UUID,
    val title: String,
    val availableAt: OffsetDateTime?,
    val questions: List<QuizQuestion>,
)

data class QuizQuestion(
    val id: UUID,
    val quizId: UUID,
    val prompt: String,
    val answer: String,
    val orderIndex: Int,
)

interface QuizRepository {
    fun listByLessonId(lessonId: UUID): List<Quiz>

    fun listAll(): List<Quiz>
}
