ALTER TABLE tb_genres
    ADD COLUMN parent_id UUID;

ALTER TABLE tb_genres
    ADD CONSTRAINT fk_genre_parent
        FOREIGN KEY (parent_id)
            REFERENCES tb_genres(id)
            ON DELETE CASCADE;