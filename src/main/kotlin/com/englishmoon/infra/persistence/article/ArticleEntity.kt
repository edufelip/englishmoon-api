package com.englishmoon.infra.persistence.article

import com.englishmoon.domain.article.Article
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "articles")
class ArticleEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false, unique = true)
    lateinit var slug: String

    @Column(nullable = false)
    lateinit var title: String

    @Column(nullable = false, columnDefinition = "text")
    lateinit var excerpt: String

    @Column
    var image: String? = null

    @Column(name = "read_time")
    var readTime: String? = null

    @Column(name = "published_on")
    var publishedOn: LocalDate? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: OffsetDateTime

    @OneToMany(
        mappedBy = "article",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    @OrderBy("orderIndex ASC, id ASC")
    var sections: MutableList<ArticleSectionEntity> = mutableListOf()

    fun toDomain(): Article =
        Article(
            slug = slug,
            title = title,
            excerpt = excerpt,
            image = image,
            readTime = readTime ?: "",
            publishedOn = publishedOn?.toString() ?: "",
            body = sections.sortedBy { it.orderIndex }.map { it.content },
        )

    companion object {
        fun fromDomain(article: Article): ArticleEntity =
            ArticleEntity().apply {
                id = UUID.randomUUID()
                slug = article.slug
                title = article.title
                excerpt = article.excerpt
                image = article.image
                readTime = article.readTime
                publishedOn = article.publishedOn.takeIf { it.isNotBlank() }?.let(LocalDate::parse)
                val now = OffsetDateTime.now()
                createdAt = now
                updatedAt = now
                sections = mutableListOf()
            }
    }
}
