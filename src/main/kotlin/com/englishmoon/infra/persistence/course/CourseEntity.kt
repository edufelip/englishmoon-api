package com.englishmoon.infra.persistence.course

import com.englishmoon.domain.course.Course
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "courses")
class CourseEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var title: String

    @Column
    var summary: String? = null

    @Column(name = "published_at")
    var publishedAt: OffsetDateTime? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: OffsetDateTime

    @PrePersist
    fun onCreate() {
        val now = OffsetDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    fun toDomain(): Course =
        Course(
            id = id,
            title = title,
            summary = summary,
            publishedAt = publishedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun fromDomain(course: Course): CourseEntity =
            CourseEntity().apply {
                id = course.id
                title = course.title
                summary = course.summary
                publishedAt = course.publishedAt
                val now = OffsetDateTime.now()
                createdAt = course.createdAt ?: now
                updatedAt = course.updatedAt ?: now
            }
    }
}
