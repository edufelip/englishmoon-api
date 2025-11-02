CREATE TABLE IF NOT EXISTS articles (
    id UUID PRIMARY KEY,
    slug VARCHAR(140) NOT NULL UNIQUE,
    title VARCHAR(240) NOT NULL,
    excerpt TEXT NOT NULL,
    image TEXT,
    read_time VARCHAR(40),
    published_on DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS article_sections (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    order_index INT NOT NULL,
    content TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_article_sections_article_id ON article_sections(article_id, order_index);
