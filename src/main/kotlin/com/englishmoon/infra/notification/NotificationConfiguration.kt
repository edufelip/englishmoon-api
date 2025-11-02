package com.englishmoon.infra.notification

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationConfiguration {
    @Bean
    fun emailSender(): EmailSender = LoggingEmailSender()
}
