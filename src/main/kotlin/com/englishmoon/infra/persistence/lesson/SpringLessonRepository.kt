package com.englishmoon.infra.persistence.lesson

import com.englishmoon.domain.lesson.Lesson
import com.englishmoon.domain.lesson.LessonRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LessonJpaRepository : JpaRepository<LessonEntity, UUID> {
    @Query(
        """
        select l from LessonEntity l 
        where l.courseId = :courseId 
        order by l.orderIndex asc, l.createdAt asc
        """,
    )
    fun findAllByCourseIdOrderByOrderIndexAsc(courseId: UUID): List<LessonEntity>

    @Query(
        """
        select l from LessonEntity l 
        order by l.courseId asc, l.orderIndex asc, l.createdAt asc
        """,
    )
    fun findAllSorted(): List<LessonEntity>
}

@Repository
class SpringLessonRepository(
    private val jpaRepository: LessonJpaRepository,
) : LessonRepository {
    override fun listByCourseId(courseId: UUID): List<Lesson> =
        jpaRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId).map { it.toDomain() }

    override fun listAll(): List<Lesson> = jpaRepository.findAllSorted().map { it.toDomain() }
}
