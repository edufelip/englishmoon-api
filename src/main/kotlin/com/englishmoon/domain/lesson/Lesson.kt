package com.englishmoon.domain.lesson

import java.time.OffsetDateTime
import java.util.UUID

data class Lesson(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val content: String?,
    val orderIndex: Int,
    val createdAt: OffsetDateTime,
)

interface LessonRepository {
    fun listByCourseId(courseId: UUID): List<Lesson>

    fun listAll(): List<Lesson>
}
