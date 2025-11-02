package com.englishmoon.infra.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "security.password-reset")
data class PasswordResetProperties(
    @DefaultValue("PT1H")
    val tokenTtl: Duration,
    @DefaultValue("http://localhost:3000/reset-password")
    val baseUrl: String,
)
