package com.englishmoon.infra.web.article

import com.englishmoon.app.article.GetArticle
import com.englishmoon.app.article.ListArticles
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/articles")
class ArticleController(
    private val listArticles: ListArticles,
    private val getArticle: GetArticle,
) {
    @GetMapping
    suspend fun index(): ArticleCollectionResponse {
        val items = listArticles.all().map(ArticleResponse::fromDomain).toList()
        return ArticleCollectionResponse(items)
    }

    @GetMapping("/{slug}")
    suspend fun show(
        @PathVariable slug: String,
    ): ResponseEntity<ArticleResponse> {
        val article = getArticle.bySlug(slug)
        return ResponseEntity.ok(ArticleResponse.fromDomain(article))
    }
}

data class ArticleCollectionResponse(
    val items: List<ArticleResponse>,
)

data class ArticleResponse(
    val slug: String,
    val title: String,
    val excerpt: String,
    val image: String?,
    val readTime: String,
    val publishedOn: String,
    val body: List<String>,
) {
    companion object {
        fun fromDomain(article: com.englishmoon.domain.article.Article): ArticleResponse =
            ArticleResponse(
                slug = article.slug,
                title = article.title,
                excerpt = article.excerpt,
                image = article.image,
                readTime = article.readTime,
                publishedOn = article.publishedOn,
                body = article.body,
            )
    }
}
