package com.englishmoon.infra.web.errors

import java.util.UUID

class CourseNotFoundException(id: UUID) : RuntimeException("Course $id not found")
