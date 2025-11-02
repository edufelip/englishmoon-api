package com.englishmoon.app.article

import com.englishmoon.domain.article.Article
import com.englishmoon.domain.article.ArticleRepository
import com.englishmoon.infra.web.errors.ArticleNotFoundException
import org.springframework.stereotype.Service

@Service
class GetArticle(
    private val repository: ArticleRepository,
) {
    suspend fun bySlug(slug: String): Article = repository.findBySlug(slug) ?: throw ArticleNotFoundException(slug)
}
