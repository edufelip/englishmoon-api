package com.englishmoon.infra.web.dashboard

import com.englishmoon.app.dashboard.GetDashboardSummary
import com.englishmoon.domain.dashboard.DashboardSummary
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val getDashboardSummary: GetDashboardSummary,
) {
    @GetMapping("/summary")
    suspend fun summary(): DashboardSummary = getDashboardSummary.retrieve()
}
