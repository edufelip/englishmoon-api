package com.englishmoon.domain.enrollment

import java.time.OffsetDateTime
import java.util.UUID

data class Enrollment(
    val id: UUID,
    val userId: UUID,
    val courseId: UUID,
    val enrolledAt: OffsetDateTime,
)

interface EnrollmentRepository {
    fun save(enrollment: Enrollment): Enrollment

    fun exists(
        userId: UUID,
        courseId: UUID,
    ): Boolean
}
