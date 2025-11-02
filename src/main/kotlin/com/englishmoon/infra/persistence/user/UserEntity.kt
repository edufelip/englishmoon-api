package com.englishmoon.infra.persistence.user

import com.englishmoon.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false, unique = true)
    lateinit var email: String

    @Column(name = "display_name", nullable = false)
    lateinit var displayName: String

    @Column(name = "password_hash", nullable = false)
    lateinit var passwordHash: String

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    @PrePersist
    fun onPersist() {
        if (!::createdAt.isInitialized) {
            createdAt = OffsetDateTime.now()
        }
    }

    fun toDomain(): User =
        User(
            id = id,
            email = email,
            displayName = displayName,
            passwordHash = passwordHash,
            createdAt = createdAt,
        )

    companion object {
        fun fromDomain(user: User): UserEntity =
            UserEntity().apply {
                id = user.id
                email = user.email
                displayName = user.displayName
                passwordHash = user.passwordHash
                createdAt = user.createdAt
            }
    }
}
