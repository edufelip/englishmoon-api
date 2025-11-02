package com.englishmoon.infra.notification

import org.slf4j.LoggerFactory

data class OutboundEmail(
    val to: String,
    val subject: String,
    val body: String,
)

fun interface EmailSender {
    fun send(email: OutboundEmail)
}

class LoggingEmailSender : EmailSender {
    private val logger = LoggerFactory.getLogger(LoggingEmailSender::class.java)

    override fun send(email: OutboundEmail) {
        logger.info(
            "Sending transactional email to {} subject '{}': {}",
            email.to,
            email.subject,
            email.body,
        )
    }
}
