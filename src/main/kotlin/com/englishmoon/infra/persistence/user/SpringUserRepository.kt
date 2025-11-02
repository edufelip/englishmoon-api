package com.englishmoon.infra.persistence.user

import com.englishmoon.domain.user.User
import com.englishmoon.domain.user.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, java.util.UUID> {
    fun existsByEmailIgnoreCase(email: String): Boolean

    fun findByEmailIgnoreCase(email: String): UserEntity?
}

@Repository
class SpringUserRepository(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        return userJpaRepository.save(entity).toDomain()
    }

    override fun existsByEmail(email: String): Boolean = userJpaRepository.existsByEmailIgnoreCase(email)

    override fun findByEmail(email: String): User? = userJpaRepository.findByEmailIgnoreCase(email)?.toDomain()

    override fun findById(id: java.util.UUID): User? = userJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
}
