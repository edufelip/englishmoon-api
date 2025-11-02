package com.englishmoon.app.user

import com.englishmoon.domain.user.User
import com.englishmoon.domain.user.UserRepository
import com.englishmoon.infra.notification.EmailSender
import com.englishmoon.infra.notification.OutboundEmail
import com.englishmoon.infra.web.errors.EmailAlreadyUsedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RegisterUser(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailSender: EmailSender,
) {
    fun handle(command: Command): User {
        if (repository.existsByEmail(command.email.lowercase())) {
            throw EmailAlreadyUsedException(command.email)
        }
        val now = OffsetDateTime.now()
        val user =
            User(
                id = UUID.randomUUID(),
                email = command.email.lowercase(),
                displayName = command.displayName,
                passwordHash = passwordEncoder.encode(command.password),
                createdAt = now,
            )
        val saved = repository.save(user)
        emailSender.send(
            OutboundEmail(
                to = saved.email,
                subject = "Welcome to EnglishMoon",
                body =
                    """
                    Hi ${saved.displayName},

                    Your EnglishMoon workspace is ready. Sign in to explore courses, lessons, and quizzes tailored to your goals.

                    — EnglishMoon Team
                    """.trimIndent(),
            ),
        )
        return saved
    }

    data class Command(
        val email: String,
        val password: String,
        val displayName: String,
    )
}
