package com.englishmoon.app.course

import com.englishmoon.domain.course.Course
import com.englishmoon.domain.course.CourseRepository
import com.englishmoon.infra.web.errors.CourseNotFoundException
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UpdateCourse(
    private val repository: CourseRepository,
) {
    fun handle(
        id: UUID,
        command: Command,
    ): Course {
        val existing = repository.findById(id) ?: throw CourseNotFoundException(id)
        val updated =
            existing.copy(
                title = command.title ?: existing.title,
                summary = if (command.hasSummary) command.summary else existing.summary,
                publishedAt = if (command.hasPublishedAt) command.publishedAt else existing.publishedAt,
            )
        return repository.save(updated)
    }

    data class Command(
        val title: String?,
        val summary: String?,
        val publishedAt: OffsetDateTime?,
        val hasSummary: Boolean,
        val hasPublishedAt: Boolean,
    )
}
