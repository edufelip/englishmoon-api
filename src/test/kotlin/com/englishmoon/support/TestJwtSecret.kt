package com.englishmoon.support

import org.springframework.test.context.DynamicPropertyRegistry
import java.security.SecureRandom
import java.util.Base64

object TestJwtSecret {
    private val cached by lazy {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        Base64.getEncoder().encodeToString(bytes)
    }

    val value: String
        get() = cached

    fun register(registry: DynamicPropertyRegistry) {
        registry.add("security.jwt.secret") { cached }
    }
}
