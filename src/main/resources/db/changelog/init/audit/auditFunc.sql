-- liquibase formatted sql

-- changeset vpetrenko:08.09.2025-16 splitStatements:false endDelimiter://
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'post_audit_log'
CREATE OR REPLACE FUNCTION audit_post_changes()
RETURNS TRIGGER AS $function$
DECLARE
    changed_data JSONB := '{}';
    old_data JSONB := '{}';
    new_data JSONB := '{}';
BEGIN
    -- Для INSERT
    IF (TG_OP = 'INSERT') THEN
        new_data := jsonb_build_object(
            'subject', NEW.subject,
            'content', NEW.content,
            'created_at', NEW.created_at,
            'is_active', NEW.is_active
        );

        INSERT INTO post_audit_log (
            post_id, operation, new_values, changed_by
        ) VALUES (
            NEW.id, 'INSERT', new_data, USER
        );
        RETURN NEW;

    -- Для UPDATE
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Собираем измененные поля
        IF OLD.subject IS DISTINCT FROM NEW.subject THEN
            changed_data := changed_data || jsonb_build_object('subject', NEW.subject);
            old_data := old_data || jsonb_build_object('subject', OLD.subject);
            new_data := new_data || jsonb_build_object('subject', NEW.subject);
        END IF;

        IF OLD.content IS DISTINCT FROM NEW.content THEN
            changed_data := changed_data || jsonb_build_object('content', NEW.content);
            old_data := old_data || jsonb_build_object('content', OLD.content);
            new_data := new_data || jsonb_build_object('content', NEW.content);
        END IF;

        IF OLD.is_active IS DISTINCT FROM NEW.is_active THEN
            changed_data := changed_data || jsonb_build_object('is_active', NEW.is_active);
            old_data := old_data || jsonb_build_object('is_active', OLD.is_active);
            new_data := new_data || jsonb_build_object('is_active', NEW.is_active);
        END IF;

        IF OLD.created_at IS DISTINCT FROM NEW.created_at THEN
            changed_data := changed_data || jsonb_build_object('created_at', NEW.created_at);
            old_data := old_data || jsonb_build_object('created_at', OLD.created_at);
            new_data := new_data || jsonb_build_object('created_at', NEW.created_at);
        END IF;

        -- Записываем только если были изменения
        IF changed_data != '{}' THEN
            INSERT INTO post_audit_log (
                post_id, operation, changed_fields, old_values, new_values,
                changed_by
            ) VALUES (
                NEW.id, 'UPDATE', changed_data, old_data, new_data, USER
            );
        END IF;
        RETURN NEW;

    -- Для DELETE
    ELSIF (TG_OP = 'DELETE') THEN
        old_data := jsonb_build_object(
            'subject', OLD.subject,
            'content', OLD.content,
            'created_at', OLD.created_at,
            'is_active', OLD.is_active
        );

        INSERT INTO post_audit_log (
            post_id, operation, old_values, changed_by
        ) VALUES (
            OLD.id, 'DELETE', old_data, USER
        );
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$function$ LANGUAGE plpgsql;
-- rollback DROP FUNCTION IF EXISTS audit_post_changes();