package com.englishmoon.app.course

import com.englishmoon.domain.course.Course
import com.englishmoon.domain.course.CourseRepository
import com.englishmoon.domain.course.PagedResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import kotlin.math.max
import kotlin.math.min

@Service
class ListCourses(
    private val repository: CourseRepository,
) {
    fun page(
        page: Int,
        size: Int,
    ): PagedResult<Course> {
        val (sanitizedPage, sanitizedSize) = sanitize(page, size)
        return repository.findPage(sanitizedPage, sanitizedSize)
    }

    fun pageFlow(
        page: Int,
        size: Int,
    ): Flow<PagedResult<Course>> {
        val (sanitizedPage, sanitizedSize) = sanitize(page, size)
        return flow {
            emit(repository.findPage(sanitizedPage, sanitizedSize))
        }
    }

    private fun sanitize(
        page: Int,
        size: Int,
    ): Pair<Int, Int> {
        val sanitizedPage = max(page, 0)
        val sanitizedSize = min(max(size, 1), MAX_PAGE_SIZE)
        return sanitizedPage to sanitizedSize
    }

    companion object {
        private const val MAX_PAGE_SIZE = 100
    }
}
