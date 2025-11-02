package com.englishmoon.infra.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class TokenService(
    private val jwtEncoder: JwtEncoder,
    @Value("\${security.jwt.access-token-ttl-minutes:15}")
    private val accessTokenTtlMinutes: Long,
    @Value("\${security.jwt.issuer:englishmoon}")
    private val issuer: String,
) {
    fun generateAccessToken(
        userId: UUID,
        email: String,
    ): TokenDescriptor {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES)
        val claims =
            JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("email", email)
                .build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
        return TokenDescriptor(token, expiresAt)
    }

    data class TokenDescriptor(
        val token: String,
        val expiresAt: Instant,
    )
}
