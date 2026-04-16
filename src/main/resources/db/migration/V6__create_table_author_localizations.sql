CREATE TABLE tb_author_localizations (
     id UUID PRIMARY KEY,
     author_id UUID NOT NULL REFERENCES tb_authors(id) ON DELETE CASCADE,
     language_code VARCHAR(5) NOT NULL,
     biography TEXT NOT NULL
);