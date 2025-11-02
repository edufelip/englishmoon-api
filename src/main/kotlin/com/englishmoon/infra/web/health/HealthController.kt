package com.englishmoon.infra.web.health

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping
    fun health(): ResponseEntity<HealthResponse> = ResponseEntity.ok(HealthResponse())

    data class HealthResponse(val status: String = "ok", val checkedAt: OffsetDateTime = OffsetDateTime.now())
}
