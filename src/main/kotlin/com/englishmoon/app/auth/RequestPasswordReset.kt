package com.englishmoon.app.auth

import com.englishmoon.domain.auth.PasswordResetToken
import com.englishmoon.domain.auth.PasswordResetTokenRepository
import com.englishmoon.domain.user.UserRepository
import com.englishmoon.infra.notification.EmailSender
import com.englishmoon.infra.notification.OutboundEmail
import com.englishmoon.infra.security.PasswordResetProperties
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.Base64
import java.util.UUID

@Service
class RequestPasswordReset(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailSender: EmailSender,
    private val properties: PasswordResetProperties,
) {
    private val secureRandom = SecureRandom()

    fun handle(email: String) {
        val user = userRepository.findByEmail(email.lowercase())
        if (user == null) {
            // Avoid leaking which emails exist; respond as if successful.
            return
        }

        passwordResetTokenRepository.deleteExpired()

        val tokenId = UUID.randomUUID()
        val verifier = generateVerifier()
        val tokenHash = hash(verifier)

        val token =
            PasswordResetToken(
                id = tokenId,
                userId = user.id,
                tokenHash = tokenHash,
                expiresAt = OffsetDateTime.now().plus(properties.tokenTtl),
                createdAt = OffsetDateTime.now(),
            )

        passwordResetTokenRepository.save(token)

        val link = "${properties.baseUrl}?token=$tokenId.$verifier"
        val subject = "Reset your EnglishMoon password"
        val body =
            """
            Hi ${user.displayName},

            We received a request to reset your EnglishMoon password. If you made this request, click the link below:

            $link

            If you didn’t ask to reset your password, you can ignore this message.

            — EnglishMoon Support
            """.trimIndent()

        emailSender.send(OutboundEmail(to = user.email, subject = subject, body = body))
    }

    private fun generateVerifier(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hash(value: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(value.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes)
    }
}
