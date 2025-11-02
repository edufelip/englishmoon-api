package com.englishmoon.infra.web.course

import com.englishmoon.app.course.CreateCourse
import com.englishmoon.app.course.GetCourse
import com.englishmoon.app.course.ListCourses
import com.englishmoon.app.course.UpdateCourse
import com.fasterxml.jackson.annotation.JsonSetter
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/courses")
class CourseController(
    private val listCourses: ListCourses,
    private val createCourse: CreateCourse,
    private val getCourse: GetCourse,
    private val updateCourse: UpdateCourse,
) {
    @GetMapping
    suspend fun index(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CourseCollectionResponse> {
        val result = listCourses.pageFlow(page, size).first()
        return ResponseEntity.ok(
            CourseCollectionResponse(
                items = result.items.map(CourseResponse::fromDomain),
                page = result.page,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                hasNext = result.hasNext,
                hasPrevious = result.hasPrevious,
            ),
        )
    }

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): Flow<CourseResponse> =
        listCourses
            .pageFlow(page, size)
            .transform { result ->
                result.items
                    .map(CourseResponse::fromDomain)
                    .forEach { emit(it) }
            }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): CourseResponse = CourseResponse.fromDomain(getCourse.byId(id))

    @PostMapping
    fun create(
        @Valid @RequestBody request: CourseRequest,
    ): ResponseEntity<CourseResponse> {
        val created = createCourse.handle(request.toCommand())
        return ResponseEntity
            .created(URI.create("/courses/${created.id}"))
            .body(CourseResponse.fromDomain(created))
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateCourseRequest,
    ): CourseResponse {
        val updated = updateCourse.handle(id, request.toCommand())
        return CourseResponse.fromDomain(updated)
    }

    data class CourseRequest(
        @field:NotBlank
        val title: String,
        val summary: String?,
        val publishedAt: OffsetDateTime?,
    ) {
        fun toCommand(): CreateCourse.Command =
            CreateCourse.Command(
                title = title,
                summary = summary,
                publishedAt = publishedAt,
            )
    }

    data class UpdateCourseRequest(
        var title: String? = null,
        var summary: String? = null,
        var publishedAt: OffsetDateTime? = null,
    ) {
        var summaryPresent: Boolean = false
            private set
        var publishedAtPresent: Boolean = false
            private set

        @JsonSetter("summary")
        fun setSummaryValue(value: String?) {
            summary = value
            summaryPresent = true
        }

        @JsonSetter("publishedAt")
        fun setPublishedAtValue(value: OffsetDateTime?) {
            publishedAt = value
            publishedAtPresent = true
        }

        fun toCommand(): UpdateCourse.Command =
            UpdateCourse.Command(
                title = title,
                summary = summary,
                publishedAt = publishedAt,
                hasSummary = summaryPresent,
                hasPublishedAt = publishedAtPresent,
            )
    }
}

data class CourseCollectionResponse(
    val items: List<CourseResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)
