package com.englishmoon.infra.persistence.auth

import com.englishmoon.domain.auth.PasswordResetToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetTokenEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "user_id", nullable = false)
    lateinit var userId: UUID

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    lateinit var tokenHash: String

    @Column(name = "expires_at", nullable = false)
    lateinit var expiresAt: OffsetDateTime

    @Column(name = "used_at")
    var usedAt: OffsetDateTime? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    fun toDomain(): PasswordResetToken =
        PasswordResetToken(
            id = id,
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            usedAt = usedAt,
            createdAt = createdAt,
        )

    companion object {
        fun fromDomain(token: PasswordResetToken): PasswordResetTokenEntity =
            PasswordResetTokenEntity().apply {
                id = token.id
                userId = token.userId
                tokenHash = token.tokenHash
                expiresAt = token.expiresAt
                usedAt = token.usedAt
                createdAt = token.createdAt
            }
    }
}
