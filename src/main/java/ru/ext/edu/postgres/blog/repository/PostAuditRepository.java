package ru.ext.edu.postgres.blog.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ext.edu.postgres.blog.entity.PostAudit;

public interface PostAuditRepository extends JpaRepository<PostAudit, Long> {
    Page<PostAudit> findByPostId(@NotNull Pageable pageable, @NotNull Long postId);
}