package com.englishmoon.domain.auth

import java.time.OffsetDateTime
import java.util.UUID

data class PasswordResetToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: OffsetDateTime,
    val usedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime,
) {
    fun isExpired(now: OffsetDateTime = OffsetDateTime.now()): Boolean = now.isAfter(expiresAt)

    fun isUsed(): Boolean = usedAt != null
}

interface PasswordResetTokenRepository {
    fun save(token: PasswordResetToken): PasswordResetToken

    fun findById(id: UUID): PasswordResetToken?

    fun markUsed(
        id: UUID,
        usedAt: OffsetDateTime = OffsetDateTime.now(),
    )

    fun deleteExpired(now: OffsetDateTime = OffsetDateTime.now())
}
