package com.englishmoon.infra.web.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class AuthControllerIT(
    private val restTemplate: TestRestTemplate,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun registerUserIfNeeded() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            }
        val payload =
            mapOf(
                "email" to "tester@example.com",
                "password" to "SecurePass123",
                "displayName" to "Tester",
            )
        val request = HttpEntity(payload, headers)
        val response = restTemplate.postForEntity("/users", request, Map::class.java)
        if (response.statusCode != HttpStatus.CREATED && response.statusCode != HttpStatus.CONFLICT) {
            throw IllegalStateException("Failed to prepare test user")
        }
    }

    @Test
    fun `should issue access token`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            }
        val payload =
            mapOf(
                "email" to "tester@example.com",
                "password" to "SecurePass123",
            )
        val request = HttpEntity(payload, headers)

        val response = restTemplate.postForEntity("/auth/login", request, Map::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertNotNull(body?.get("accessToken"))
        assertEquals("Bearer", body?.get("tokenType"))
        val setCookie = response.headers.getFirst(HttpHeaders.SET_COOKIE)
        assertNotNull(setCookie)
        assertTrue(setCookie!!.contains("englishmoon_refresh"))

        val refreshHeaders =
            HttpHeaders().apply {
                accept = listOf(MediaType.APPLICATION_JSON)
                add(HttpHeaders.COOKIE, setCookie)
            }
        val refreshResponse =
            restTemplate.postForEntity(
                "/auth/refresh",
                HttpEntity<Void>(null, refreshHeaders),
                Map::class.java,
            )
        assertEquals(HttpStatus.OK, refreshResponse.statusCode)
        val refreshBody = refreshResponse.body
        assertNotNull(refreshBody?.get("accessToken"))
        assertEquals("Bearer", refreshBody?.get("tokenType"))
    }

    @Test
    fun `should reject invalid credentials`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            }
        val payload =
            mapOf(
                "email" to "tester@example.com",
                "password" to "WrongPassword!",
            )
        val request = HttpEntity(payload, headers)

        val response = restTemplate.postForEntity("/auth/login", request, Map::class.java)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `should reject invalid refresh cookie`() {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.add(HttpHeaders.COOKIE, "englishmoon_refresh=invalid")

        val response =
            restTemplate.postForEntity(
                "/auth/refresh",
                HttpEntity<Void>(null, headers),
                Map::class.java,
            )

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `should rate limit repeated login attempts`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            }
        val payload =
            mapOf(
                "email" to "tester@example.com",
                "password" to "WrongPassword!",
            )
        val request = HttpEntity(payload, headers)

        repeat(20) {
            val response = restTemplate.postForEntity("/auth/login", request, Map::class.java)
            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        }

        val throttled = restTemplate.postForEntity("/auth/login", request, Map::class.java)
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
