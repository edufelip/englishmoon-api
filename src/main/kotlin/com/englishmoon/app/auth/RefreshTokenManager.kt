package com.englishmoon.app.auth

import com.englishmoon.domain.auth.RefreshToken
import com.englishmoon.domain.auth.RefreshTokenRepository
import com.englishmoon.infra.web.errors.InvalidRefreshTokenException
import com.englishmoon.infra.web.errors.RefreshTokenExpiredException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.Base64
import java.util.UUID

@Service
class RefreshTokenManager(
    private val repository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${security.jwt.refresh-token-ttl-days:30}")
    private val refreshTtlDays: Long,
    @Value("\${security.jwt.max-refresh-tokens-per-user:5}")
    private val maxTokensPerUser: Int,
) {
    fun issue(userId: UUID): Result {
        val rawToken = generateToken()
        val now = OffsetDateTime.now()
        val token =
            RefreshToken(
                id = UUID.randomUUID(),
                userId = userId,
                tokenHash = passwordEncoder.encode(rawToken),
                issuedAt = now,
                expiresAt = now.plusDays(refreshTtlDays),
            )
        repository.save(token)
        enforceLimit(userId)
        return Result(token.id, rawToken, token.expiresAt, userId)
    }

    fun rotate(
        tokenId: UUID,
        rawToken: String,
    ): Result {
        val existing = repository.findById(tokenId) ?: throw InvalidRefreshTokenException()
        if (existing.isRevoked()) {
            throw InvalidRefreshTokenException()
        }
        if (existing.isExpired()) {
            repository.revoke(existing.id)
            throw RefreshTokenExpiredException()
        }
        if (!passwordEncoder.matches(rawToken, existing.tokenHash)) {
            repository.revoke(existing.id)
            throw InvalidRefreshTokenException()
        }

        repository.revoke(existing.id)
        return issue(existing.userId)
    }

    private fun enforceLimit(userId: UUID) {
        val activeTokens =
            repository.findActiveByUserId(userId)
                .sortedByDescending { it.issuedAt }
        if (activeTokens.size > maxTokensPerUser) {
            activeTokens.drop(maxTokensPerUser).forEach { repository.revoke(it.id) }
        }
    }

    private fun generateToken(): String {
        val buffer = ByteArray(TOKEN_BYTE_LENGTH)
        secureRandom.nextBytes(buffer)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer)
    }

    data class Result(
        val id: UUID,
        val rawToken: String,
        val expiresAt: OffsetDateTime,
        val userId: UUID,
    ) {
        fun toCookieValue(): String = "$id.$rawToken"
    }

    companion object {
        private const val TOKEN_BYTE_LENGTH = 64
        private val secureRandom = SecureRandom()
    }
}
