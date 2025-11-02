package com.englishmoon.app.course

import com.englishmoon.domain.course.Course
import com.englishmoon.domain.course.CourseRepository
import com.englishmoon.infra.web.errors.CourseNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetCourse(
    private val repository: CourseRepository,
) {
    fun byId(id: UUID): Course = repository.findById(id) ?: throw CourseNotFoundException(id)
}
