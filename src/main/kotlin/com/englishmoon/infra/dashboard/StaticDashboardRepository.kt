package com.englishmoon.infra.dashboard

import com.englishmoon.domain.dashboard.ActionItem
import com.englishmoon.domain.dashboard.DashboardRepository
import com.englishmoon.domain.dashboard.DashboardSummary
import com.englishmoon.domain.dashboard.UpcomingSession
import org.springframework.stereotype.Component

@Component
class StaticDashboardRepository : DashboardRepository {
    override suspend fun summary(): DashboardSummary =
        DashboardSummary(
            completionRate = 82,
            weeklyMinutes = 145,
            streakWeeks = 6,
            upcomingSessions =
                listOf(
                    UpcomingSession(
                        id = "1",
                        title = "Speaking lab: Client presentation",
                        date = "2025-08-01T14:00:00Z",
                        coach = "Maria Gomez",
                    ),
                    UpcomingSession(
                        id = "2",
                        title = "Grammar workshop: Advanced conditionals",
                        date = "2025-08-03T18:00:00Z",
                        coach = "James Wilson",
                    ),
                ),
            actionItems =
                listOf(
                    ActionItem(
                        id = "a1",
                        label = "Record pronunciation drill for \"th\" and \"w\" sounds",
                        due = "Tomorrow",
                    ),
                    ActionItem(
                        id = "a2",
                        label = "Review feedback from last speaking lab",
                        due = "This week",
                    ),
                ),
        )
}
