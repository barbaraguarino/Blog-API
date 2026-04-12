CREATE TABLE tb_genre_translations (
   id UUID PRIMARY KEY,
   genre_id UUID NOT NULL,
   language_code VARCHAR(10) NOT NULL,
   name VARCHAR(150) NOT NULL,
   CONSTRAINT fk_genre_translation FOREIGN KEY (genre_id) REFERENCES tb_genres(id) ON DELETE CASCADE,
   CONSTRAINT uk_language_name UNIQUE (language_code, name),
   CONSTRAINT chk_language_code_format CHECK (language_code ~ '^[a-z]{2}-[A-Z]{2}$')

);

CREATE INDEX idx_genre_translation_name ON tb_genre_translations(name);