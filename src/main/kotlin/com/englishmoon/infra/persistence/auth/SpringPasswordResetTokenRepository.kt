package com.englishmoon.infra.persistence.auth

import com.englishmoon.domain.auth.PasswordResetToken
import com.englishmoon.domain.auth.PasswordResetTokenRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface PasswordResetTokenJpaRepository : JpaRepository<PasswordResetTokenEntity, UUID> {
    @Modifying
    @Transactional
    @Query("delete from PasswordResetTokenEntity t where t.expiresAt < :cutoff")
    fun deleteExpired(cutoff: OffsetDateTime)
}

@Repository
class SpringPasswordResetTokenRepository(
    private val jpaRepository: PasswordResetTokenJpaRepository,
) : PasswordResetTokenRepository {
    override fun save(token: PasswordResetToken): PasswordResetToken =
        jpaRepository.save(PasswordResetTokenEntity.fromDomain(token)).toDomain()

    override fun findById(id: UUID): PasswordResetToken? = jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    @Transactional
    override fun markUsed(
        id: UUID,
        usedAt: OffsetDateTime,
    ) {
        val entity =
            jpaRepository.findById(id).orElse(null)
                ?: return
        entity.usedAt = usedAt
        jpaRepository.save(entity)
    }

    override fun deleteExpired(now: OffsetDateTime) {
        jpaRepository.deleteExpired(now)
    }
}
