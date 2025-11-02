package com.englishmoon.infra.web.course

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

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
class CourseControllerIT(
    private val restTemplate: TestRestTemplate,
    @Autowired private val jwtEncoder: JwtEncoder,
) {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `should create list and retrieve a course`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
                setBearerAuth(jwt())
            }

        val payload =
            mapOf(
                "title" to "Grammar Foundations",
                "summary" to "Understand sentence structure",
                "publishedAt" to "2024-01-01T00:00:00Z",
            )

        val createResponse =
            restTemplate.postForEntity(
                "/courses",
                HttpEntity(payload, headers),
                CourseResponse::class.java,
            )

        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val createdCourse = createResponse.body
        assertNotNull(createdCourse)

        val listResponse =
            restTemplate.exchange(
                "/courses?page=0&size=10",
                HttpMethod.GET,
                HttpEntity<Void>(null, HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_JSON) }),
                object : ParameterizedTypeReference<CourseCollectionResponse>() {},
            )
        assertEquals(HttpStatus.OK, listResponse.statusCode)
        val body = listResponse.body
        val courses = body?.items.orEmpty()
        assertEquals(1, courses.size)
        assertEquals(1L, body?.totalElements)
        assertEquals(false, body?.hasNext)

        val fetchedResponse = restTemplate.getForEntity("/courses/${createdCourse!!.id}", CourseResponse::class.java)
        assertEquals(HttpStatus.OK, fetchedResponse.statusCode)
        assertEquals("Grammar Foundations", fetchedResponse.body?.title)
    }

    @Test
    fun `should return 404 for missing course`() {
        val response = restTemplate.getForEntity("/courses/${UUID.randomUUID()}", CourseResponse::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    private fun jwt(): String {
        val claims =
            JwtClaimsSet.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue
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
