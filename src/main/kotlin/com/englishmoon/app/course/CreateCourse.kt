package com.englishmoon.app.course

import com.englishmoon.domain.course.Course
import com.englishmoon.domain.course.CourseRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CreateCourse(
    private val repository: CourseRepository,
) {
    fun handle(command: Command): Course {
        val course =
            Course(
                id = UUID.randomUUID(),
                title = command.title,
                summary = command.summary,
                publishedAt = command.publishedAt,
            )
        return repository.save(course)
    }

    data class Command(
        val title: String,
        val summary: String?,
        val publishedAt: OffsetDateTime?,
    )
}
