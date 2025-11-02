package com.englishmoon.infra.persistence.quiz

import com.englishmoon.domain.quiz.Quiz
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "quizzes")
class QuizEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "lesson_id", nullable = false)
    lateinit var lessonId: UUID

    @Column(nullable = false)
    lateinit var title: String

    @Column(name = "available_at")
    var availableAt: OffsetDateTime? = null

    @OneToMany(
        mappedBy = "quiz",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    @OrderBy("orderIndex ASC, id ASC")
    var questions: MutableList<QuizQuestionEntity> = mutableListOf()

    fun toDomain(): Quiz =
        Quiz(
            id = id,
            lessonId = lessonId,
            title = title,
            availableAt = availableAt,
            questions =
                questions
                    .sortedWith(
                        compareBy<QuizQuestionEntity> { it.orderIndex }.thenBy { it.id },
                    ).map(QuizQuestionEntity::toDomain),
        )

    companion object {
        fun fromDomain(quiz: Quiz): QuizEntity =
            QuizEntity().apply {
                id = quiz.id
                lessonId = quiz.lessonId
                title = quiz.title
                availableAt = quiz.availableAt
                questions =
                    quiz.questions
                        .map { question ->
                            QuizQuestionEntity().apply {
                                id = question.id
                                prompt = question.prompt
                                answer = question.answer
                                orderIndex = question.orderIndex
                            }
                        }.toMutableList()
                questions.forEach { it.quiz = this }
            }
    }
}
