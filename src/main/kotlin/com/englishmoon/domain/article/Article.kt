package com.englishmoon.domain.article

import kotlinx.coroutines.flow.Flow

data class Article(
    val slug: String,
    val title: String,
    val excerpt: String,
    val image: String?,
    val readTime: String,
    val publishedOn: String,
    val body: List<String>,
)

interface ArticleRepository {
    fun list(): Flow<Article>

    suspend fun findBySlug(slug: String): Article?
}
