--liquibase formatted sql

-- changeset vpetrenko:08.09.2025-01 comment: Создание таблицы пользователей (SQL нотация не поддерживает комменты, оставил только для читаемости changeset в коде)
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users'
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_user_login ON users(login);
-- rollback DROP TABLE IF EXISTS users CASCADE;

-- changeset vpetrenko:08.09.2025-02 comment: Создание таблицы постов
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'posts'
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    content TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
CREATE INDEX IF NOT EXISTS idx_posts_is_active ON posts(created_at);
-- rollback DROP TABLE IF EXISTS posts CASCADE;

-- changeset vpetrenko:08.09.2025-03 comment: Создание таблицы тегов
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tags'
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
-- rollback DROP TABLE IF EXISTS tags CASCADE;

-- changeset vpetrenko:08.09.2025-04 comment: Создание таблицы комментариев
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'comments'
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);
-- rollback DROP TABLE IF EXISTS comments CASCADE;

-- changeset vpetrenko:08.09.2025-05 comment: Создание таблицы лайков
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'likes'
CREATE TABLE likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT uk_likes_user_post UNIQUE (user_id, post_id)
);
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes(user_id);
CREATE INDEX IF NOT EXISTS idx_likes_post_id ON likes(post_id);
-- rollback DROP TABLE IF EXISTS likes CASCADE;

-- changeset vpetrenko:08.09.2025-06 comment: Создание таблицы связи постов и тегов
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'post_tags'
CREATE TABLE post_tags (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_post_tags_tag_id ON post_tags(tag_id);
-- rollback DROP TABLE IF EXISTS post_tags CASCADE;