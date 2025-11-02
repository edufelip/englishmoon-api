package com.englishmoon.infra.persistence.quiz

import com.englishmoon.domain.quiz.Quiz
import com.englishmoon.domain.quiz.QuizRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuizJpaRepository : JpaRepository<QuizEntity, UUID> {
    @EntityGraph(attributePaths = ["questions"])
    @Query("select q from QuizEntity q where q.lessonId = :lessonId order by q.title asc")
    fun findAllByLessonId(lessonId: UUID): List<QuizEntity>

    @EntityGraph(attributePaths = ["questions"])
    @Query("select q from QuizEntity q order by q.lessonId asc, q.title asc")
    fun findAllWithQuestions(): List<QuizEntity>
}

@Repository
class SpringQuizRepository(
    private val jpaRepository: QuizJpaRepository,
) : QuizRepository {
    override fun listByLessonId(lessonId: UUID): List<Quiz> =
        jpaRepository
            .findAllByLessonId(lessonId)
            .map { it.toDomain() }

    override fun listAll(): List<Quiz> =
        jpaRepository
            .findAllWithQuestions()
            .map { it.toDomain() }
}
