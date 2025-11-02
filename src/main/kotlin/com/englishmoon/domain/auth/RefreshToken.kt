package com.englishmoon.domain.auth

import java.time.OffsetDateTime
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val issuedAt: OffsetDateTime,
    val expiresAt: OffsetDateTime,
    val revokedAt: OffsetDateTime? = null,
) {
    fun isExpired(now: OffsetDateTime = OffsetDateTime.now()): Boolean = now.isAfter(expiresAt)

    fun isRevoked(): Boolean = revokedAt != null
}

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken

    fun findActiveByUserId(userId: UUID): List<RefreshToken>

    fun findById(id: UUID): RefreshToken?

    fun revoke(
        tokenId: UUID,
        revokedAt: OffsetDateTime = OffsetDateTime.now(),
    )
}
