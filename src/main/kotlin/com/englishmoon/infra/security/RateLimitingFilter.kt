package com.englishmoon.infra.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class RateLimitingFilter(
    private val properties: RateLimitProperties,
) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(RateLimitingFilter::class.java)
    private val buckets = ConcurrentHashMap<String, Counter>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val rule = findRule(request.requestURI)
        if (rule == null) {
            filterChain.doFilter(request, response)
            return
        }

        val key = buildKey(rule, request)
        val now = Instant.now()

        val counter =
            buckets.compute(key) { _, existing ->
                val windowStart = existing?.windowStart ?: now
                val windowExpires = windowStart.plus(rule.window)
                if (now.isAfter(windowExpires)) {
                    Counter(count = 1, windowStart = now)
                } else {
                    val nextCount = (existing?.count ?: 0) + 1
                    Counter(count = nextCount, windowStart = windowStart)
                }
            } ?: Counter(count = 1, windowStart = now)

        if (counter.count > rule.maxRequests) {
            logger.debug("Rate limit exceeded for path {} key {}", rule.path, key)
            writeTooManyRequests(response)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun findRule(path: String): RateLimitProperties.Rule? {
        return properties.rules().firstOrNull { rule -> path.startsWith(rule.path) }
    }

    private fun buildKey(
        rule: RateLimitProperties.Rule,
        request: HttpServletRequest,
    ): String {
        val forwarded = request.getHeader("X-Forwarded-For")?.split(',')?.firstOrNull()?.trim()
        val remote = forwarded?.takeIf { it.isNotBlank() } ?: request.remoteAddr
        return "${rule.path}:$remote"
    }

    private fun writeTooManyRequests(response: HttpServletResponse) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.use { writer ->
            writer.write(
                """
                {"status":429,"error":"Too Many Requests","message":"Too many attempts. Try again later."}
                """.trimIndent(),
            )
        }
    }

    data class Counter(
        val count: Int,
        val windowStart: Instant,
    )
}
