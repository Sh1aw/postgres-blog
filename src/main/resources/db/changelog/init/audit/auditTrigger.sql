-- liquibase formatted sql

-- changeset vpetrenko:08.09.2025-17 comment: Создание триггера для аудита изменений постов
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = current_schema() AND p.proname = 'audit_post_changes'
CREATE TRIGGER trigger_audit_post_changes
    AFTER INSERT OR UPDATE OR DELETE ON posts
    FOR EACH ROW EXECUTE FUNCTION audit_post_changes()
-- rollback DROP TRIGGER IF EXISTS audit_post_changes_trigger ON posts;