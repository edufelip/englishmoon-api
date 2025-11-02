package com.englishmoon.infra.persistence.enrollment

import com.englishmoon.domain.enrollment.Enrollment
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "enrollments")
class EnrollmentEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "user_id", nullable = false)
    lateinit var userId: UUID

    @Column(name = "course_id", nullable = false)
    lateinit var courseId: UUID

    @Column(name = "enrolled_at", nullable = false)
    lateinit var enrolledAt: OffsetDateTime

    fun toDomain(): Enrollment =
        Enrollment(
            id = id,
            userId = userId,
            courseId = courseId,
            enrolledAt = enrolledAt,
        )

    companion object {
        fun fromDomain(enrollment: Enrollment): EnrollmentEntity =
            EnrollmentEntity().apply {
                id = enrollment.id
                userId = enrollment.userId
                courseId = enrollment.courseId
                enrolledAt = enrollment.enrolledAt
            }
    }
}
