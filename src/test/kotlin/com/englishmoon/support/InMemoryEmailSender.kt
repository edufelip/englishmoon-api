package com.englishmoon.support

import com.englishmoon.infra.notification.EmailSender
import com.englishmoon.infra.notification.OutboundEmail
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryEmailSender : EmailSender {
    private val emails = CopyOnWriteArrayList<OutboundEmail>()

    override fun send(email: OutboundEmail) {
        emails += email
    }

    fun clear() {
        emails.clear()
    }

    fun sentEmails(): List<OutboundEmail> = emails.toList()
}

@Configuration
class TestEmailSenderConfiguration {
    private val sender = InMemoryEmailSender()

    @Bean
    @Primary
    fun inMemoryEmailSender(): EmailSender = sender

    @Bean
    fun exposedInMemoryEmailSender(): InMemoryEmailSender = sender
}
