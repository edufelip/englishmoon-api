package com.englishmoon.app.enrollment

import com.englishmoon.domain.course.CourseRepository
import com.englishmoon.domain.enrollment.Enrollment
import com.englishmoon.domain.enrollment.EnrollmentRepository
import com.englishmoon.domain.user.UserRepository
import com.englishmoon.infra.web.errors.CourseNotFoundException
import com.englishmoon.infra.web.errors.DuplicateEnrollmentException
import com.englishmoon.infra.web.errors.UserNotFoundException
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CreateEnrollment(
    private val enrollmentRepository: EnrollmentRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
) {
    fun handle(command: Command): Enrollment {
        val user =
            userRepository.findById(command.userId)
                ?: throw UserNotFoundException(command.userId)
        val course =
            courseRepository.findById(command.courseId)
                ?: throw CourseNotFoundException(command.courseId)

        if (enrollmentRepository.exists(user.id, course.id)) {
            throw DuplicateEnrollmentException()
        }

        val enrollment =
            Enrollment(
                id = UUID.randomUUID(),
                userId = user.id,
                courseId = course.id,
                enrolledAt = OffsetDateTime.now(),
            )
        return enrollmentRepository.save(enrollment)
    }

    data class Command(
        val userId: UUID,
        val courseId: UUID,
    )
}
