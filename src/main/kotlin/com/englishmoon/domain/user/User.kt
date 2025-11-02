package com.englishmoon.domain.user

import java.time.OffsetDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val createdAt: OffsetDateTime,
)

interface UserRepository {
    fun save(user: User): User

    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun findById(id: UUID): User?
}
