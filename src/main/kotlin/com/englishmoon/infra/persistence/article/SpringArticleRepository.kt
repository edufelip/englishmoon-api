package com.englishmoon.infra.persistence.article

import com.englishmoon.domain.article.Article
import com.englishmoon.domain.article.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleJpaRepository : JpaRepository<ArticleEntity, java.util.UUID> {
    @EntityGraph(attributePaths = ["sections"])
    fun findBySlug(slug: String): ArticleEntity?

    @EntityGraph(attributePaths = ["sections"])
    fun findAllByOrderByPublishedOnDesc(): List<ArticleEntity>
}

@Repository
class SpringArticleRepository(
    private val jpaRepository: ArticleJpaRepository,
) : ArticleRepository {
    override fun list(): Flow<Article> =
        flow {
            jpaRepository
                .findAllByOrderByPublishedOnDesc()
                .forEach { emit(it.toDomain()) }
        }

    override suspend fun findBySlug(slug: String): Article? = jpaRepository.findBySlug(slug)?.toDomain()
}
