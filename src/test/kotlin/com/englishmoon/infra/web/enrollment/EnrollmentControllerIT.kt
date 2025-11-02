package com.englishmoon.infra.web.enrollment

import com.englishmoon.infra.persistence.course.CourseEntity
import com.englishmoon.infra.persistence.course.CourseJpaRepository
import com.englishmoon.infra.persistence.enrollment.EnrollmentJpaRepository
import com.englishmoon.infra.persistence.user.UserEntity
import com.englishmoon.infra.persistence.user.UserJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import java.time.OffsetDateTime
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
class EnrollmentControllerIT(
    private val restTemplate: TestRestTemplate,
    private val userRepository: UserJpaRepository,
    private val courseRepository: CourseJpaRepository,
    private val enrollmentRepository: EnrollmentJpaRepository,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun cleanup() {
        enrollmentRepository.deleteAll()
        userRepository.deleteAll()
        courseRepository.deleteAll()
    }

    @Test
    fun `should create enrollment`() {
        val user = persistUser()
        val course = persistCourse()

        val request = EnrollmentController.EnrollmentRequest(userId = user.id, courseId = course.id)
        val response: ResponseEntity<EnrollmentController.EnrollmentResponse> =
            restTemplate.postForEntity(
                URI.create("http://localhost:$port/enrollments"),
                request,
                EnrollmentController.EnrollmentResponse::class.java,
            )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        val body = response.body!!
        assertEquals(user.id, body.userId)
        assertEquals(course.id, body.courseId)
        assertEquals(1, enrollmentRepository.count())
    }

    @Test
    fun `should return conflict when enrollment already exists`() {
        val user = persistUser()
        val course = persistCourse()

        val request = EnrollmentController.EnrollmentRequest(userId = user.id, courseId = course.id)
        restTemplate.postForEntity(
            URI.create("http://localhost:$port/enrollments"),
            request,
            EnrollmentController.EnrollmentResponse::class.java,
        )

        val duplicateResponse =
            restTemplate.exchange(
                RequestEntity.post(URI.create("http://localhost:$port/enrollments")).body(request),
                EnrollmentController.EnrollmentResponse::class.java,
            )

        assertEquals(HttpStatus.CONFLICT, duplicateResponse.statusCode)
    }

    private fun persistUser(): UserEntity {
        val entity =
            UserEntity().apply {
                id = UUID.randomUUID()
                email = "learner-${UUID.randomUUID()}@example.com"
                displayName = "Learner"
                passwordHash = "hashed"
            }
        return userRepository.save(entity)
    }

    private fun persistCourse(): CourseEntity {
        val entity =
            CourseEntity().apply {
                id = UUID.randomUUID()
                title = "Course ${UUID.randomUUID()}"
                summary = "Summary"
                publishedAt = OffsetDateTime.now()
            }
        return courseRepository.save(entity)
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
