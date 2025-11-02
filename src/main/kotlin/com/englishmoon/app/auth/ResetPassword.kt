package com.englishmoon.app.auth

import com.englishmoon.domain.auth.PasswordResetTokenRepository
import com.englishmoon.domain.user.UserRepository
import com.englishmoon.infra.notification.EmailSender
import com.englishmoon.infra.notification.OutboundEmail
import com.englishmoon.infra.security.PasswordResetProperties
import com.englishmoon.infra.web.errors.PasswordResetTokenExpiredException
import com.englishmoon.infra.web.errors.PasswordResetTokenInvalidException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Base64
import java.util.UUID

@Service
class ResetPassword(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailSender: EmailSender,
    private val properties: PasswordResetProperties,
) {
    fun handle(
        rawToken: String,
        newPassword: String,
    ) {
        val (tokenId, verifier) = parseToken(rawToken)
        val token =
            passwordResetTokenRepository.findById(tokenId)
                ?: throw PasswordResetTokenInvalidException()

        if (token.isUsed()) {
            throw PasswordResetTokenInvalidException()
        }
        if (token.isExpired()) {
            throw PasswordResetTokenExpiredException()
        }

        val expectedHash = hash(verifier)
        if (token.tokenHash != expectedHash) {
            throw PasswordResetTokenInvalidException()
        }

        val user =
            userRepository.findById(token.userId)
                ?: throw PasswordResetTokenInvalidException()

        val encoded = passwordEncoder.encode(newPassword)
        val updatedUser = user.copy(passwordHash = encoded)
        userRepository.save(updatedUser)

        passwordResetTokenRepository.markUsed(token.id, OffsetDateTime.now())
        passwordResetTokenRepository.deleteExpired()

        notifyUser(user.email)
    }

    private fun notifyUser(email: String) {
        val subject = "Your EnglishMoon password was changed"
        val body =
            """
            Hi,

            This is a confirmation that your EnglishMoon password was successfully changed. If you did not perform this action, initiate another password reset immediately.

            — EnglishMoon Support
            """.trimIndent()
        emailSender.send(OutboundEmail(to = email, subject = subject, body = body))
    }

    private fun parseToken(raw: String): Pair<UUID, String> {
        val parts = raw.split('.', limit = 2)
        if (parts.size != 2) {
            throw PasswordResetTokenInvalidException()
        }
        val id =
            try {
                UUID.fromString(parts[0])
            } catch (ex: IllegalArgumentException) {
                throw PasswordResetTokenInvalidException()
            }
        val verifier = parts[1]
        if (verifier.isBlank()) {
            throw PasswordResetTokenInvalidException()
        }
        return id to verifier
    }

    private fun hash(value: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(value.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes)
    }
}
