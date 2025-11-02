package com.englishmoon.infra.security

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RateLimitingConfiguration {
    @Bean
    fun rateLimitingFilterRegistration(properties: RateLimitProperties): FilterRegistrationBean<RateLimitingFilter> {
        val registration = FilterRegistrationBean(RateLimitingFilter(properties))
        registration.order = 1
        return registration
    }
}
