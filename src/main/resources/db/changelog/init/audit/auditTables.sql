-- changeset vpetrenko:08.09.2025-12 comment: Создание таблицы аудита изменений постов
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'post_audit_log'
CREATE TABLE post_audit_log (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    operation VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    changed_fields JSONB, -- Список измененных полей (NULL для INSERT/DELETE)
    old_values JSONB,     -- Старые значения (NULL для INSERT)
    new_values JSONB,     -- Новые значения (NULL для DELETE)
    changed_by TEXT,      -- Кто изменил (если доступно)
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() -- Время изменения
);
-- rollback DROP TABLE IF EXISTS post_audit_log CASCADE;

-- changeset vpetrenko:08.09.2025-13 comment: Создание индекса для поиска по ID поста в аудите
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'post_audit_log'
CREATE INDEX idx_post_audit_post_id ON post_audit_log(post_id);
-- rollback DROP INDEX IF EXISTS idx_post_audit_post_id;

-- changeset vpetrenko:08.09.2025-14 comment: Создание индекса для поиска по типу операции в аудите
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'post_audit_log'
CREATE INDEX idx_post_audit_operation ON post_audit_log(operation);
-- rollback DROP INDEX IF EXISTS idx_post_audit_operation;

-- changeset vpetrenko:08.09.2025-15 comment: Создание индекса для поиска по времени изменения в аудите
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'post_audit_log'
CREATE INDEX idx_post_audit_changed_at ON post_audit_log(changed_at);
-- rollback DROP INDEX IF EXISTS idx_post_audit_changed_at;