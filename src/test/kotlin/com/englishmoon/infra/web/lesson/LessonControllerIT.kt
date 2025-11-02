package com.englishmoon.infra.web.lesson

import com.englishmoon.infra.persistence.course.CourseEntity
import com.englishmoon.infra.persistence.course.CourseJpaRepository
import com.englishmoon.infra.persistence.lesson.LessonEntity
import com.englishmoon.infra.persistence.lesson.LessonJpaRepository
import com.englishmoon.support.TestJwtSecret
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
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
class LessonControllerIT(
    private val restTemplate: TestRestTemplate,
    private val lessonRepository: LessonJpaRepository,
    private val courseRepository: CourseJpaRepository,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        lessonRepository.deleteAll()
        courseRepository.deleteAll()
    }

    @Test
    fun `should list lessons filtered by course`() {
        val course = persistCourse()
        val otherCourse = persistCourse(title = "Writing Clinic")

        val lessonOne = persistLesson(course.id, "Intro", order = 1)
        val lessonTwo = persistLesson(course.id, "Dialogue", order = 2)
        persistLesson(otherCourse.id, "Irrelevant", order = 1)

        val response =
            restTemplate.getForEntity(
                "http://localhost:$port/lessons?courseId=${course.id}",
                Array<LessonController.LessonResponse>::class.java,
            )

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body.orEmpty()
        assertEquals(2, body.size)
        assertEquals(listOf(lessonOne.id, lessonTwo.id), body.map { it.id })
    }

    private fun persistCourse(title: String = "Pronunciation Lab"): CourseEntity {
        val entity =
            CourseEntity().apply {
                id = UUID.randomUUID()
                this.title = title
                summary = "Summary for $title"
                publishedAt = OffsetDateTime.now()
            }
        return courseRepository.save(entity)
    }

    private fun persistLesson(
        courseId: UUID,
        title: String,
        order: Int,
    ): LessonEntity {
        val entity =
            LessonEntity().apply {
                id = UUID.randomUUID()
                this.courseId = courseId
                this.title = title
                content = "Content for $title"
                orderIndex = order
            }
        return lessonRepository.save(entity)
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
