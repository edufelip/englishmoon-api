package com.englishmoon.app.lesson

import com.englishmoon.domain.lesson.Lesson
import com.englishmoon.domain.lesson.LessonRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ListLessons(
    private val lessonRepository: LessonRepository,
) {
    fun list(courseId: UUID?): List<Lesson> =
        if (courseId != null) {
            lessonRepository.listByCourseId(courseId)
        } else {
            lessonRepository.listAll()
        }
}
