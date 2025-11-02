package com.englishmoon.infra.persistence.auth

import com.englishmoon.domain.auth.RefreshToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "user_id", nullable = false)
    lateinit var userId: UUID

    @Column(name = "token_hash", nullable = false)
    lateinit var tokenHash: String

    @Column(name = "issued_at", nullable = false)
    lateinit var issuedAt: OffsetDateTime

    @Column(name = "expires_at", nullable = false)
    lateinit var expiresAt: OffsetDateTime

    @Column(name = "revoked_at")
    var revokedAt: OffsetDateTime? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    fun toDomain(): RefreshToken =
        RefreshToken(
            id = id,
            userId = userId,
            tokenHash = tokenHash,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            revokedAt = revokedAt,
        )

    companion object {
        fun fromDomain(token: RefreshToken): RefreshTokenEntity =
            RefreshTokenEntity().apply {
                id = token.id
                userId = token.userId
                tokenHash = token.tokenHash
                issuedAt = token.issuedAt
                expiresAt = token.expiresAt
                revokedAt = token.revokedAt
                createdAt = token.issuedAt
            }
    }
}
