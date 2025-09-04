--liquibase formatted sql

-- changeset vpetrenko:08.09.2025-data-ru-1
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 select count(*) from users;
-- precondition-sql-check expectedResult:0 select count(*) from tags;
-- precondition-sql-check expectedResult:0 select count(*) from posts;
-- precondition-sql-check expectedResult:0 select count(*) from post_tags;
-- precondition-sql-check expectedResult:0 select count(*) from comments;
-- precondition-sql-check expectedResult:0 select count(*) from likes;
INSERT INTO users (email, login) VALUES
('ivanov@example.com', 'ivanov'),
('petrov@example.com', 'petrov'),
('sidorova@example.com', 'sidorova');

INSERT INTO tags (name) VALUES
('Технологии'),
('Наука'),
('Здоровье'),
('Путешествия'),
('Еда');

INSERT INTO posts (subject, content, user_id, created_at, is_active) VALUES
('Первый пост', 'Это содержание первого поста.', 1, NOW() - INTERVAL '10 days', TRUE),
('Второй пост', 'Содержание второго поста.', 2, NOW() - INTERVAL '5 days', TRUE),
('Третий пост', 'Еще один интересный пост.', 1, NOW() - INTERVAL '2 days', TRUE),
('Архивный пост', 'Этот пост неактивен.', 3, NOW() - INTERVAL '1 day', FALSE);

INSERT INTO post_tags (post_id, tag_id) VALUES
(1, 1), -- Первый пост -> Технологии
(1, 2), -- Первый пост -> Наука
(2, 3), -- Второй пост -> Здоровье
(3, 1), -- Третий пост -> Технологии
(3, 4); -- Третий пост -> Путешествия

INSERT INTO comments (text, created_at, post_id, user_id, is_active) VALUES
('Отличный пост!', NOW() - INTERVAL '9 days', 1, 2, TRUE),
('Спасибо за информацию.', NOW() - INTERVAL '8 days', 1, 3, TRUE),
('Интересная точка зрения.', NOW() - INTERVAL '4 days', 2, 1, TRUE),
('У меня вопрос по этой теме.', NOW() - INTERVAL '3 days', 2, 3, TRUE),
('Полезная статья, спасибо!', NOW() - INTERVAL '1 day', 3, 2, TRUE),
('Комментарий к архивному посту.', NOW(), 4, 1, TRUE); -- Комментарий к архивному посту

INSERT INTO likes (user_id, post_id) VALUES
(2, 1), -- petrov лайкает Первый пост
(3, 1), -- sidorova лайкает Первый пост
(1, 2), -- ivanov лайкает Второй пост
(3, 3); -- sidorova лайкает Третий пост
