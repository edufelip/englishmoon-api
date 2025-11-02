package com.englishmoon.app.quiz

import com.englishmoon.domain.quiz.Quiz
import com.englishmoon.domain.quiz.QuizRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ListQuizzes(
    private val quizRepository: QuizRepository,
) {
    fun list(lessonId: UUID?): List<Quiz> =
        if (lessonId != null) {
            quizRepository.listByLessonId(lessonId)
        } else {
            quizRepository.listAll()
        }
}
