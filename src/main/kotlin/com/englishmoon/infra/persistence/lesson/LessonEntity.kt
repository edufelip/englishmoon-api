package com.englishmoon.infra.persistence.lesson

import com.englishmoon.domain.lesson.Lesson
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "lessons")
class LessonEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "course_id", nullable = false)
    lateinit var courseId: UUID

    @Column(nullable = false)
    lateinit var title: String

    @Column(columnDefinition = "text")
    var content: String? = null

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    @PrePersist
    fun onCreate() {
        if (!::createdAt.isInitialized) {
            createdAt = OffsetDateTime.now()
        }
    }

    fun toDomain(): Lesson =
        Lesson(
            id = id,
            courseId = courseId,
            title = title,
            content = content,
            orderIndex = orderIndex,
            createdAt = createdAt,
        )

    companion object {
        fun fromDomain(lesson: Lesson): LessonEntity =
            LessonEntity().apply {
                id = lesson.id
                courseId = lesson.courseId
                title = lesson.title
                content = lesson.content
                orderIndex = lesson.orderIndex
                createdAt = lesson.createdAt
            }
    }
}
