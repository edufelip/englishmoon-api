package com.englishmoon.infra.web.enrollment

import com.englishmoon.app.enrollment.CreateEnrollment
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/enrollments")
class EnrollmentController(
    private val createEnrollment: CreateEnrollment,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: EnrollmentRequest,
    ): ResponseEntity<EnrollmentResponse> {
        val enrollment =
            createEnrollment.handle(
                CreateEnrollment.Command(
                    userId = request.userId,
                    courseId = request.courseId,
                ),
            )
        val body = EnrollmentResponse.fromDomain(enrollment)
        return ResponseEntity
            .created(URI.create("/enrollments/${body.id}"))
            .body(body)
    }

    data class EnrollmentRequest(
        @field:NotNull
        val userId: UUID,
        @field:NotNull
        val courseId: UUID,
    )

    data class EnrollmentResponse(
        val id: UUID,
        val userId: UUID,
        val courseId: UUID,
        val enrolledAt: OffsetDateTime,
    ) {
        companion object {
            fun fromDomain(enrollment: com.englishmoon.domain.enrollment.Enrollment): EnrollmentResponse =
                EnrollmentResponse(
                    id = enrollment.id,
                    userId = enrollment.userId,
                    courseId = enrollment.courseId,
                    enrolledAt = enrollment.enrolledAt,
                )
        }
    }
}
