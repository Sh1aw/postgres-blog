package ru.ext.edu.postgres.blog.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.ext.edu.postgres.blog.api.AuditApi;
import ru.ext.edu.postgres.blog.model.PagePostAudit;
import ru.ext.edu.postgres.blog.service.AuditService;

@RestController
@RequiredArgsConstructor
public class AuditController implements AuditApi {
    private final AuditService auditService;

    @Override
    public ResponseEntity<PagePostAudit> auditPostIdGet(Long postId, Pageable pageable) {
        return ResponseEntity.ok(auditService.getAudit(pageable, postId));
    }
}
