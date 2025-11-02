package com.englishmoon.infra.web.auth

import com.englishmoon.infra.persistence.auth.PasswordResetTokenJpaRepository
import com.englishmoon.infra.persistence.user.UserEntity
import com.englishmoon.infra.persistence.user.UserJpaRepository
import com.englishmoon.support.InMemoryEmailSender
import com.englishmoon.support.TestJwtSecret
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Tag("integration")
@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.hikari.maximum-pool-size=1",
    ],
)
class PasswordResetControllerIT(
    private val restTemplate: TestRestTemplate,
    private val userRepository: UserJpaRepository,
    private val passwordResetTokenRepository: PasswordResetTokenJpaRepository,
    private val inMemoryEmailSender: InMemoryEmailSender,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        passwordResetTokenRepository.deleteAll()
        userRepository.deleteAll()
        inMemoryEmailSender.clear()
    }

    @Test
    fun `should issue reset email and accept reset`() {
        val user = persistUser(email = "learner@example.com", password = "old-password-hash")

        val forgotRequest =
            HttpEntity(
                mapOf("email" to user.email),
                HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON },
            )
        val forgotResponse =
            restTemplate.postForEntity(
                "http://localhost:$port/auth/forgot-password",
                forgotRequest,
                Void::class.java,
            )

        assertEquals(HttpStatus.ACCEPTED, forgotResponse.statusCode)

        val email = inMemoryEmailSender.sentEmails().single()
        assertTrue(email.body.contains("reset"))
        val token = email.body.substringAfter("token=").trim().split("\n").first()

        val resetRequest =
            HttpEntity(
                mapOf("token" to token, "password" to "NewPass123!"),
                HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON },
            )
        val resetResponse =
            restTemplate.postForEntity(
                "http://localhost:$port/auth/reset-password",
                resetRequest,
                Void::class.java,
            )

        assertEquals(HttpStatus.NO_CONTENT, resetResponse.statusCode)
    }

    @Test
    fun `should apply rate limiting on password reset requests`() {
        val user = persistUser(email = "limited@example.com", password = "hash")
        val request =
            HttpEntity(
                mapOf("email" to user.email),
                HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON },
            )

        repeat(5) {
            val response =
                restTemplate.postForEntity(
                    "http://localhost:$port/auth/forgot-password",
                    request,
                    Void::class.java,
                )
            assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        }

        val throttled =
            restTemplate.postForEntity(
                "http://localhost:$port/auth/forgot-password",
                request,
                String::class.java,
            )

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, throttled.statusCode)
    }

    private fun persistUser(
        email: String,
        password: String,
    ): UserEntity {
        val entity =
            UserEntity().apply {
                id = UUID.randomUUID()
                this.email = email
                displayName = "Learner"
                passwordHash = password
                createdAt = OffsetDateTime.now()
            }
        return userRepository.save(entity)
    }

    companion object {
        @Container
        val postgres: PostgreSQLContainer<Nothing> =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("englishmoon_test")
                withUsername("postgres")
                withPassword("postgres")
            }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            TestJwtSecret.register(registry)
        }
    }
}
