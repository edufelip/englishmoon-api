package com.englishmoon.infra.web.auth

import com.englishmoon.app.auth.RefreshTokenManager
import com.englishmoon.app.user.AuthenticateUser
import com.englishmoon.domain.user.UserRepository
import com.englishmoon.infra.security.TokenService
import com.englishmoon.infra.web.errors.InvalidRefreshTokenException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticateUser: AuthenticateUser,
    private val tokenService: TokenService,
    private val refreshTokenManager: RefreshTokenManager,
    private val userRepository: UserRepository,
    @Value("\${security.jwt.refresh-cookie-secure:true}")
    private val refreshCookieSecure: Boolean,
    @Value("\${security.jwt.refresh-cookie-name:englishmoon_refresh}")
    private val refreshCookieName: String,
    @Value("\${security.jwt.refresh-cookie-same-site:Lax}")
    private val refreshCookieSameSite: String,
    @Value("\${security.jwt.refresh-cookie-domain:}")
    private val refreshCookieDomain: String,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthResponse> {
        val user = authenticateUser.handle(request.toCommand())
        val tokenDescriptor = tokenService.generateAccessToken(user.id, user.email)
        val refresh = refreshTokenManager.issue(user.id)
        val refreshCookie = buildRefreshCookie(refresh)

        val response =
            AuthResponse(
                accessToken = tokenDescriptor.token,
                expiresAt = tokenDescriptor.expiresAt,
                user =
                    AuthUser(
                        id = user.id.toString(),
                        email = user.email,
                        displayName = user.displayName,
                    ),
            )
        return ResponseEntity.status(HttpStatus.OK)
            .header("Set-Cookie", refreshCookie.toString())
            .body(response)
    }

    @PostMapping("/refresh")
    fun refresh(request: HttpServletRequest): ResponseEntity<AuthResponse> {
        val cookieValue =
            request.cookies
                ?.firstOrNull { it.name == refreshCookieName }
                ?.value
        if (cookieValue.isNullOrBlank()) {
            throw InvalidRefreshTokenException()
        }

        val (tokenId, raw) = parseRefreshCookie(cookieValue)
        val rotated = refreshTokenManager.rotate(tokenId, raw)
        val user = userRepository.findById(rotated.userId) ?: throw InvalidRefreshTokenException()
        val accessToken = tokenService.generateAccessToken(user.id, user.email)
        val refreshCookie = buildRefreshCookie(rotated)

        val response =
            AuthResponse(
                accessToken = accessToken.token,
                expiresAt = accessToken.expiresAt,
                user =
                    AuthUser(
                        id = user.id.toString(),
                        email = user.email,
                        displayName = user.displayName,
                    ),
            )

        return ResponseEntity.status(HttpStatus.OK)
            .header("Set-Cookie", refreshCookie.toString())
            .body(response)
    }

    data class LoginRequest(
        @field:Email
        @field:NotBlank
        val email: String,
        @field:NotBlank
        val password: String,
    ) {
        fun toCommand(): AuthenticateUser.Command =
            AuthenticateUser.Command(
                email = email,
                password = password,
            )
    }

    data class AuthResponse(
        val accessToken: String,
        val expiresAt: Instant,
        val tokenType: String = "Bearer",
        val user: AuthUser,
    )

    data class AuthUser(
        val id: String,
        val email: String,
        val displayName: String,
    )

    private fun parseRefreshCookie(cookie: String): Pair<UUID, String> {
        val parts = cookie.split('.', limit = 2)
        if (parts.size != 2) {
            throw InvalidRefreshTokenException()
        }
        val id =
            try {
                UUID.fromString(parts[0])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRefreshTokenException()
            }
        val raw = parts[1]
        if (raw.isBlank()) {
            throw InvalidRefreshTokenException()
        }
        return id to raw
    }

    private fun buildRefreshCookie(refresh: RefreshTokenManager.Result): ResponseCookie {
        val duration = Duration.between(Instant.now(), refresh.expiresAt.toInstant())
        val maxAge = if (duration.isNegative) Duration.ZERO else duration
        val builder =
            ResponseCookie.from(refreshCookieName, refresh.toCookieValue())
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path("/")
                .sameSite(refreshCookieSameSite)
                .maxAge(maxAge)

        if (refreshCookieDomain.isNotBlank()) {
            builder.domain(refreshCookieDomain)
        }
        return builder.build()
    }
}
