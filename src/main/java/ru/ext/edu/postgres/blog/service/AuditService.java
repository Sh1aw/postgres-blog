package ru.ext.edu.postgres.blog.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.ext.edu.postgres.blog.mapper.PostAuditMapper;
import ru.ext.edu.postgres.blog.model.PageMetadata;
import ru.ext.edu.postgres.blog.model.PagePostAudit;

import ru.ext.edu.postgres.blog.repository.PostAuditRepository;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final PostAuditRepository postAuditRepository;
    private final PostAuditMapper postAuditMapper;

    public PagePostAudit getAudit(@NotNull Pageable pageable, @NotNull Long postId) {
        var posts = postAuditRepository.findByPostId(pageable, postId);

        return new PagePostAudit().metadata(
                new PageMetadata()
                        .number(posts.getNumber())
                        .totalElements(posts.getTotalElements())
                        .size(posts.getSize())
                        .totalPages(posts.getTotalPages())
        ).content(posts.stream().map(
                postAuditMapper::toPostAuditDto
        ).toList());
    }
}
