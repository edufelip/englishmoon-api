package com.englishmoon.infra.persistence.quiz

import com.englishmoon.domain.quiz.QuizQuestion
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "quiz_questions")
class QuizQuestionEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "prompt", nullable = false, columnDefinition = "text")
    lateinit var prompt: String

    @Column(name = "answer", nullable = false, columnDefinition = "text")
    lateinit var answer: String

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    lateinit var quiz: QuizEntity

    fun toDomain(): QuizQuestion =
        QuizQuestion(
            id = id,
            quizId = quiz.id,
            prompt = prompt,
            answer = answer,
            orderIndex = orderIndex,
        )
}
