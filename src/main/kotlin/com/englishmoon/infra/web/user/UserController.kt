package com.englishmoon.infra.web.user

import com.englishmoon.app.user.RegisterUser
import com.englishmoon.domain.user.User
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
    private val registerUser: RegisterUser,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: RegisterUserRequest,
    ): UserResponse {
        val created = registerUser.handle(request.toCommand())
        return UserResponse.fromDomain(created)
    }

    data class RegisterUserRequest(
        @field:Email
        @field:NotBlank
        val email: String,
        @field:NotBlank
        @field:Size(min = 8, message = "Password must be at least 8 characters long")
        val password: String,
        @field:NotBlank
        val displayName: String,
    ) {
        fun toCommand(): RegisterUser.Command =
            RegisterUser.Command(
                email = email,
                password = password,
                displayName = displayName,
            )
    }

    data class UserResponse(
        val id: UUID,
        val email: String,
        val displayName: String,
        val createdAt: OffsetDateTime,
    ) {
        companion object {
            fun fromDomain(user: User): UserResponse =
                UserResponse(
                    id = user.id,
                    email = user.email,
                    displayName = user.displayName,
                    createdAt = user.createdAt,
                )
        }
    }
}
