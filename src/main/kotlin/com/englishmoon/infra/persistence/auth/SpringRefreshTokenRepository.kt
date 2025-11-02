package com.englishmoon.infra.persistence.auth

import com.englishmoon.domain.auth.RefreshToken
import com.englishmoon.domain.auth.RefreshTokenRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(
        userId: UUID,
        now: OffsetDateTime,
    ): List<RefreshTokenEntity>

    fun findByIdAndRevokedAtIsNull(id: UUID): RefreshTokenEntity?

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked_at = :revokedAt WHERE id = :id AND revoked_at IS NULL")
    fun revokeById(
        id: UUID,
        revokedAt: OffsetDateTime,
    )
}

@Repository
class SpringRefreshTokenRepository(
    private val jpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(token: RefreshToken): RefreshToken {
        val entity = RefreshTokenEntity.fromDomain(token)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findActiveByUserId(userId: UUID): List<RefreshToken> =
        jpaRepository.findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfter(userId, OffsetDateTime.now())
            .map { it.toDomain() }

    override fun findById(id: UUID): RefreshToken? = jpaRepository.findByIdAndRevokedAtIsNull(id)?.toDomain()

    @Transactional
    override fun revoke(
        tokenId: UUID,
        revokedAt: OffsetDateTime,
    ) {
        jpaRepository.revokeById(tokenId, revokedAt)
    }
}
