package com.englishmoon.infra.web.quiz

import com.englishmoon.infra.persistence.course.CourseEntity
import com.englishmoon.infra.persistence.course.CourseJpaRepository
import com.englishmoon.infra.persistence.lesson.LessonEntity
import com.englishmoon.infra.persistence.lesson.LessonJpaRepository
import com.englishmoon.infra.persistence.quiz.QuizEntity
import com.englishmoon.infra.persistence.quiz.QuizJpaRepository
import com.englishmoon.infra.persistence.quiz.QuizQuestionEntity
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
class QuizControllerIT(
    private val restTemplate: TestRestTemplate,
    private val quizRepository: QuizJpaRepository,
    private val lessonRepository: LessonJpaRepository,
    private val courseRepository: CourseJpaRepository,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun cleanup() {
        quizRepository.deleteAll()
        lessonRepository.deleteAll()
        courseRepository.deleteAll()
    }

    @Test
    fun `should list quizzes by lesson`() {
        val lesson = persistLesson()
        val otherLesson = persistLesson(title = "Listening", courseTitle = "Listening Lab")

        val quiz = persistQuiz(lesson.id, "Pronunciation Basics")
        persistQuiz(otherLesson.id, "Listening Warmup")

        val response =
            restTemplate.getForEntity(
                "http://localhost:$port/quizzes?lessonId=${lesson.id}",
                Array<QuizController.QuizResponse>::class.java,
            )

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body.orEmpty()
        assertEquals(1, body.size)
        assertEquals(quiz.id, body.first().id)
        assertEquals(2, body.first().questions.size)
    }

    private fun persistLesson(
        title: String = "Speaking Lab",
        courseTitle: String = "Speaking Foundations",
    ): LessonEntity {
        val course =
            CourseEntity().apply {
                id = UUID.randomUUID()
                this.title = courseTitle
                summary = "Summary for $courseTitle"
                publishedAt = OffsetDateTime.now()
            }
        courseRepository.save(course)

        val lesson =
            LessonEntity().apply {
                id = UUID.randomUUID()
                courseId = course.id
                this.title = title
                content = "Lesson content"
                orderIndex = 1
            }
        return lessonRepository.save(lesson)
    }

    private fun persistQuiz(
        lessonId: UUID,
        title: String,
    ): QuizEntity {
        val quiz =
            QuizEntity().apply {
                id = UUID.randomUUID()
                this.lessonId = lessonId
                this.title = title
                availableAt = OffsetDateTime.now()
            }

        val questionOne =
            QuizQuestionEntity().apply {
                id = UUID.randomUUID()
                prompt = "How do you pronounce TH?"
                answer = "Place your tongue between your teeth"
                orderIndex = 0
                this.quiz = quiz
            }
        val questionTwo =
            QuizQuestionEntity().apply {
                id = UUID.randomUUID()
                prompt = "Which sentence uses present perfect?"
                answer = "I have studied English for two years."
                orderIndex = 1
                this.quiz = quiz
            }
        quiz.questions = mutableListOf(questionOne, questionTwo)
        return quizRepository.save(quiz)
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
