package com.englishmoon.app.article

import com.englishmoon.domain.article.Article
import com.englishmoon.domain.article.ArticleRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class ListArticles(
    private val repository: ArticleRepository,
) {
    fun all(): Flow<Article> = repository.list()
}
