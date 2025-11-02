package com.englishmoon.infra.web.quiz

import com.englishmoon.app.quiz.ListQuizzes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/quizzes")
class QuizController(
    private val listQuizzes: ListQuizzes,
) {
    @GetMapping
    fun index(
        @RequestParam(required = false) lessonId: UUID?,
    ): List<QuizResponse> =
        listQuizzes
            .list(lessonId)
            .map(QuizResponse::fromDomain)

    data class QuizResponse(
        val id: UUID,
        val lessonId: UUID,
        val title: String,
        val availableAt: OffsetDateTime?,
        val questions: List<QuizQuestionResponse>,
    ) {
        companion object {
            fun fromDomain(quiz: com.englishmoon.domain.quiz.Quiz): QuizResponse =
                QuizResponse(
                    id = quiz.id,
                    lessonId = quiz.lessonId,
                    title = quiz.title,
                    availableAt = quiz.availableAt,
                    questions = quiz.questions.map(QuizQuestionResponse::fromDomain),
                )
        }
    }

    data class QuizQuestionResponse(
        val id: UUID,
        val prompt: String,
        val answer: String,
        val order: Int,
    ) {
        companion object {
            fun fromDomain(question: com.englishmoon.domain.quiz.QuizQuestion): QuizQuestionResponse =
                QuizQuestionResponse(
                    id = question.id,
                    prompt = question.prompt,
                    answer = question.answer,
                    order = question.orderIndex,
                )
        }
    }
}
