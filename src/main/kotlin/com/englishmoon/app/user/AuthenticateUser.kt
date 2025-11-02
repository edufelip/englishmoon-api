package com.englishmoon.app.user

import com.englishmoon.domain.user.User
import com.englishmoon.domain.user.UserRepository
import com.englishmoon.infra.web.errors.InvalidCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticateUser(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun handle(command: Command): User {
        val user =
            repository.findByEmail(command.email.lowercase())
                ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(command.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        return user
    }

    data class Command(
        val email: String,
        val password: String,
    )
}
