-- changeset vpetrenko:08.09.2025-07 comment: Добавление колонки search_vector в таблицу posts
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'posts'
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'posts' AND column_name = 'search_vector'
ALTER TABLE posts ADD COLUMN search_vector tsvector;
-- rollback ALTER TABLE posts DROP COLUMN IF EXISTS search_vector;

-- changeset vpetrenko:08.09.2025-08 comment: Заполнение вектора полнотекстового поиска
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'posts' AND column_name = 'search_vector'
UPDATE posts SET search_vector =
    setweight(to_tsvector('russian', coalesce(subject, '')), 'A') ||
    setweight(to_tsvector('russian', coalesce(content, '')), 'B');

-- changeset vpetrenko:08.09.2025-09 comment: Создание GIN индекса для полнотекстового поиска
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'posts' AND column_name = 'search_vector'
CREATE INDEX idx_posts_search_vector ON posts USING GIN (search_vector);
-- rollback DROP INDEX IF EXISTS idx_posts_search_vector;

-- changeset vpetrenko:08.09.2025-10 comment: Создание функции для автоматического обновления вектора поиска
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'posts' AND column_name = 'search_vector'
CREATE OR REPLACE FUNCTION update_post_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('russian', coalesce(NEW.subject, '')), 'A') ||
        setweight(to_tsvector('russian', coalesce(NEW.content, '')), 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;
-- rollback DROP FUNCTION IF EXISTS update_post_search_vector();

-- changeset vpetrenko:08.09.2025-11 comment: Создание триггера для автоматического обновления вектора поиска
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = current_schema() AND p.proname = 'update_post_search_vector'
CREATE TRIGGER trigger_update_post_search_vector
    BEFORE INSERT OR UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION update_post_search_vector();
-- rollback DROP TRIGGER IF EXISTS trigger_update_post_search_vector ON posts;