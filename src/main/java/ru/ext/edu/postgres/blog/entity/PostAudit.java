package ru.ext.edu.postgres.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_audit_log")
public class PostAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_audit_log_id_seq")
    @SequenceGenerator(name = "post_audit_log_id_seq", sequenceName = "post_audit_log_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "post_id", unique = false, nullable = false)
    private Long postId;

    @Column(name = "operation", unique = false, nullable = false)
    private String operation;

    @Column(name = "changed_fields", unique = false, nullable = true, columnDefinition = "jsonb")
    private String changedFields;

    @Column(name = "old_values", unique = false, nullable = true, columnDefinition = "jsonb")
    private String oldValues;

    @Column(name = "new_values", unique = false, nullable = true, columnDefinition = "jsonb")
    private String newValues;

    @Column(name = "changed_by", unique = false, nullable = true)
    private String changedBy;

    @Column(name = "changed_at", unique = false, nullable = true)
    private OffsetDateTime changedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PostAudit postAudit = (PostAudit) o;
        return getId() != null && Objects.equals(getId(), postAudit.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
