package com.englishmoon.infra.web.auth

import com.englishmoon.app.auth.RequestPasswordReset
import com.englishmoon.app.auth.ResetPassword
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class PasswordResetController(
    private val requestPasswordReset: RequestPasswordReset,
    private val resetPassword: ResetPassword,
) {
    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody request: ForgotPasswordRequest,
    ): ResponseEntity<Void> {
        requestPasswordReset.handle(request.email)
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest,
    ): ResponseEntity<Void> {
        resetPassword.handle(request.token, request.password)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    data class ForgotPasswordRequest(
        @field:Email
        @field:NotBlank
        val email: String,
    )

    data class ResetPasswordRequest(
        @field:NotBlank
        val token: String,
        @field:NotBlank
        @field:Size(min = 8, max = 72)
        val password: String,
    )
}
