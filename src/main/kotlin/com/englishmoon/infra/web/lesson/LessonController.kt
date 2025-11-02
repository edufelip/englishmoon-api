package com.englishmoon.infra.web.lesson

import com.englishmoon.app.lesson.ListLessons
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/lessons")
class LessonController(
    private val listLessons: ListLessons,
) {
    @GetMapping
    fun index(
        @RequestParam(required = false) courseId: UUID?,
    ): List<LessonResponse> =
        listLessons
            .list(courseId)
            .map(LessonResponse::fromDomain)

    data class LessonResponse(
        val id: UUID,
        val courseId: UUID,
        val title: String,
        val content: String?,
        val order: Int,
    ) {
        companion object {
            fun fromDomain(lesson: com.englishmoon.domain.lesson.Lesson): LessonResponse =
                LessonResponse(
                    id = lesson.id,
                    courseId = lesson.courseId,
                    title = lesson.title,
                    content = lesson.content,
                    order = lesson.orderIndex,
                )
        }
    }
}
