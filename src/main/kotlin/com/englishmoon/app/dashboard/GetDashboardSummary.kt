package com.englishmoon.app.dashboard

import com.englishmoon.domain.dashboard.DashboardRepository
import com.englishmoon.domain.dashboard.DashboardSummary
import org.springframework.stereotype.Service

@Service
class GetDashboardSummary(
    private val repository: DashboardRepository,
) {
    suspend fun retrieve(): DashboardSummary = repository.summary()
}
