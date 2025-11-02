package com.englishmoon.infra.persistence.article

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "article_sections")
class ArticleSectionEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0

    @Column(nullable = false, columnDefinition = "text")
    lateinit var content: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    lateinit var article: ArticleEntity

    companion object {
        fun fromDomain(
            section: String,
            articleEntity: ArticleEntity,
            orderIndex: Int,
        ): ArticleSectionEntity =
            ArticleSectionEntity().apply {
                id = UUID.randomUUID()
                this.orderIndex = orderIndex
                content = section
                article = articleEntity
            }
    }
}
