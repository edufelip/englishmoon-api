package com.englishmoon.domain.course

import java.time.OffsetDateTime
import java.util.UUID

data class Course(
    val id: UUID,
    val title: String,
    val summary: String?,
    val publishedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)

interface CourseRepository {
    fun save(course: Course): Course

    fun findAll(): List<Course>

    fun findById(id: UUID): Course?

    fun findPage(
        page: Int,
        size: Int,
    ): PagedResult<Course>
}

data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)
