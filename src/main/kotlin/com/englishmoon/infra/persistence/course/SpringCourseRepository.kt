package com.englishmoon.infra.persistence.course

import com.englishmoon.domain.course.Course
import com.englishmoon.domain.course.CourseRepository
import com.englishmoon.domain.course.PagedResult
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CourseJpaRepository : JpaRepository<CourseEntity, UUID>

@Repository
class SpringCourseRepository(
    private val jpaRepository: CourseJpaRepository,
) : CourseRepository {
    override fun save(course: Course): Course {
        val entity = CourseEntity.fromDomain(course)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findAll(): List<Course> = jpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Course? = jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findPage(
        page: Int,
        size: Int,
    ): PagedResult<Course> {
        val pageable = PageRequest.of(page, size)
        val result = jpaRepository.findAll(pageable)
        return PagedResult(
            items = result.content.map { it.toDomain() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            hasNext = result.hasNext(),
            hasPrevious = result.hasPrevious(),
        )
    }
}
