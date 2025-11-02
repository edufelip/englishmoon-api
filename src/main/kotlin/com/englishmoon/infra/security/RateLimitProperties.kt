package com.englishmoon.infra.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "security.rate-limit")
data class RateLimitProperties(
    val forgotPassword: Rule = Rule(path = "/auth/forgot-password", maxRequests = 5, window = Duration.ofMinutes(10)),
    val login: Rule = Rule(path = "/auth/login", maxRequests = 20, window = Duration.ofMinutes(1)),
    val signup: Rule = Rule(path = "/users", maxRequests = 10, window = Duration.ofMinutes(5)),
    val extra: List<Rule> = emptyList(),
) {
    data class Rule(
        val path: String = "",
        val maxRequests: Int = 0,
        val window: Duration = Duration.ZERO,
    )

    fun rules(): List<Rule> {
        val defaults =
            listOf(
                ensureDefaults(forgotPassword, "/auth/forgot-password", 5, Duration.ofMinutes(10)),
                ensureDefaults(login, "/auth/login", 20, Duration.ofMinutes(1)),
                ensureDefaults(signup, "/users", 10, Duration.ofMinutes(5)),
            )
        val merged = linkedMapOf<String, Rule>()
        defaults.forEach { merged[it.path] = it }
        extra.filter { it.path.isNotBlank() }.forEach { merged[it.path] = it }
        return merged.values.toList()
    }

    private fun ensureDefaults(
        rule: Rule,
        defaultPath: String,
        defaultMax: Int,
        defaultWindow: Duration,
    ): Rule =
        Rule(
            path = rule.path.ifBlank { defaultPath },
            maxRequests = if (rule.maxRequests <= 0) defaultMax else rule.maxRequests,
            window = if (rule.window.isZero || rule.window.isNegative) defaultWindow else rule.window,
        )
}
