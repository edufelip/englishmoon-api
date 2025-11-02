package com.englishmoon.infra.web.errors

class ArticleNotFoundException(slug: String) : RuntimeException("Article $slug not found")
