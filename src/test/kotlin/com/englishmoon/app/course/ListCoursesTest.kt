package com.englishmoon.app.course

import com.englishmoon.domain.course.Course
import com.englishmoon.domain.course.CourseRepository
import com.englishmoon.domain.course.PagedResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class ListCoursesTest {
    private val repository = RecordingCourseRepository()
    private val useCase = ListCourses(repository)

    @Test
    fun `sanitizes negative page and large size`() {
        useCase.page(page = -5, size = 500)

        assertEquals(0, repository.lastPage)
        assertEquals(100, repository.lastSize)
    }

    @Test
    fun `returns repository result`() {
        val course =
            Course(
                id = UUID.randomUUID(),
                title = "Grammar Basics",
                summary = null,
                publishedAt = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )
        repository.nextResult =
            PagedResult(
                items = listOf(course),
                page = 2,
                size = 10,
                totalElements = 25,
                totalPages = 3,
                hasNext = true,
                hasPrevious = true,
            )

        val result = useCase.page(page = 2, size = 10)

        assertSame(repository.nextResult, result)
    }

    private class RecordingCourseRepository : CourseRepository {
        var lastPage: Int? = null
        var lastSize: Int? = null
        var nextResult: PagedResult<Course> =
            PagedResult(emptyList(), 0, 20, 0, 0, hasNext = false, hasPrevious = false)

        override fun save(course: Course): Course = throw UnsupportedOperationException()

        override fun findAll(): List<Course> = throw UnsupportedOperationException()

        override fun findById(id: UUID): Course? = throw UnsupportedOperationException()

        override fun findPage(
            page: Int,
            size: Int,
        ): PagedResult<Course> {
            lastPage = page
            lastSize = size
            return nextResult
        }
    }
}
