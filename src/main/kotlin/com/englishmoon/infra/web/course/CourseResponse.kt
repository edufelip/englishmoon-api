package com.englishmoon.infra.web.course

import com.englishmoon.domain.course.Course
import java.time.OffsetDateTime
import java.util.UUID

data class CourseResponse(
    val id: UUID,
    val title: String,
    val summary: String?,
    val publishedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
) {
    companion object {
        fun fromDomain(course: Course): CourseResponse =
            CourseResponse(
                id = course.id,
                title = course.title,
                summary = course.summary,
                publishedAt = course.publishedAt,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
            )
    }
}
