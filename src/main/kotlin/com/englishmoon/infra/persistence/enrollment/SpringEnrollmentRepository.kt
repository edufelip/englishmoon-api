package com.englishmoon.infra.persistence.enrollment

import com.englishmoon.domain.enrollment.Enrollment
import com.englishmoon.domain.enrollment.EnrollmentRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EnrollmentJpaRepository : JpaRepository<EnrollmentEntity, UUID> {
    fun existsByUserIdAndCourseId(
        userId: UUID,
        courseId: UUID,
    ): Boolean
}

@Repository
class SpringEnrollmentRepository(
    private val jpaRepository: EnrollmentJpaRepository,
) : EnrollmentRepository {
    override fun save(enrollment: Enrollment): Enrollment {
        val entity = EnrollmentEntity.fromDomain(enrollment)
        return jpaRepository.save(entity).toDomain()
    }

    override fun exists(
        userId: UUID,
        courseId: UUID,
    ): Boolean = jpaRepository.existsByUserIdAndCourseId(userId, courseId)
}
