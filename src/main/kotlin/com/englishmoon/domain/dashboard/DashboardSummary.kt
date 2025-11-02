package com.englishmoon.domain.dashboard

data class DashboardSummary(
    val completionRate: Int,
    val weeklyMinutes: Int,
    val streakWeeks: Int,
    val upcomingSessions: List<UpcomingSession>,
    val actionItems: List<ActionItem>,
)

data class UpcomingSession(
    val id: String,
    val title: String,
    val date: String,
    val coach: String,
)

data class ActionItem(
    val id: String,
    val label: String,
    val due: String,
)

interface DashboardRepository {
    suspend fun summary(): DashboardSummary
}
