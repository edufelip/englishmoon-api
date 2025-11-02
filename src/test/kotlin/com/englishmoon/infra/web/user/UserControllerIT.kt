package com.englishmoon.infra.web.user

import com.englishmoon.infra.persistence.user.UserJpaRepository
import com.englishmoon.support.InMemoryEmailSender
import org.junit.jupiter.api.Assertions.assertEquals
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Tag("integration")
@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.hikari.maximum-pool-size=1",
        "security.jwt.secret=***REMOVED***",
    ],
)
class UserControllerIT(
    private val restTemplate: TestRestTemplate,
    private val emailSender: InMemoryEmailSender,
    private val userRepository: UserJpaRepository,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun clearEmails() {
        emailSender.clear()
    }

    @Test
    fun `should register user and reject duplicate`() {
        val body =
            mapOf(
                "email" to "learner@example.com",
                "password" to "ChangeMe123",
                "displayName" to "Learner",
            )

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            }
        val request = HttpEntity(body, headers)

        val response = restTemplate.postForEntity("/users", request, Map::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode)

        val duplicate = restTemplate.postForEntity("/users", request, Map::class.java)
        assertEquals(HttpStatus.CONFLICT, duplicate.statusCode)
    }

    @Test
    fun `should rate limit excessive signup attempts`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val request =
            HttpEntity(
                mapOf(
                    "email" to "signup-test@example.com",
                    "password" to "ChangeMe123",
                    "displayName" to "Learner",
                ),
                headers,
            )

        repeat(10) {
            val response = restTemplate.postForEntity("/users", request, Map::class.java)
            assertEquals(HttpStatus.CREATED, response.statusCode)
            userRepository.deleteAll()
        }

        val throttled = restTemplate.postForEntity("/users", request, Map::class.java)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, throttled.statusCode)
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
        }
    }
}
