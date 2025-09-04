package ru.ext.edu.postgres.blog.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ext.edu.postgres.blog.entity.Comment;
import ru.ext.edu.postgres.blog.mapper.CommentMapper;
import ru.ext.edu.postgres.blog.model.PageComment;
import ru.ext.edu.postgres.blog.model.PageMetadata;
import ru.ext.edu.postgres.blog.repository.CommentRepository;
import ru.ext.edu.postgres.blog.model.CreateCommentRequest;
import ru.ext.edu.postgres.blog.model.UpdateCommentRequest;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final UserService userService;
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public Long addComment(@NotNull CreateCommentRequest createCommentRequest) {
        var post = postService.getPostEntityById(createCommentRequest.getPostId());
        var author = userService.getUserEntityById(createCommentRequest.getAuthorId());

        var comment = new Comment()
                .setPost(post)
                .setUser(author)
                .setContent(createCommentRequest.getContent());

        return commentRepository.save(comment).getId();
    }

    @Transactional
    public Long updateComment(@NotNull UpdateCommentRequest updateCommentRequest, @NotNull Long commentId) {
        var comment = commentRepository.findById(commentId).orElseThrow();
        if (!Objects.equals(updateCommentRequest.getAuthorId(), comment.getUser().getId())) {
            throw new IllegalArgumentException();
        }
        if (!userService.isUserExist(updateCommentRequest.getAuthorId())) {
            throw new IllegalArgumentException();
        }

        return commentRepository.save(
                comment.setContent(updateCommentRequest.getContent())
        ).getId();
    }

    public void hideComment(@NotNull Long commentId) {
        var comment = commentRepository.findById(commentId).orElseThrow();
        commentRepository.save(comment.setActive(false));
    }

    public PageComment getAllPostComments(@NotNull Pageable pageable, @NotNull Long postId) {
        var foundComments = findCommentsByPostIdWithUser(postId, pageable);
        return new PageComment().metadata(
                new PageMetadata()
                        .number(foundComments.getNumber())
                        .totalElements(foundComments.getTotalElements())
                        .size(foundComments.getSize())
                        .totalPages(foundComments.getTotalPages())
        ).content(foundComments.stream().map(
                commentMapper::toCommentDto
        ).toList());
    }

    private Page<Comment> findCommentsByPostIdWithUser(@NotNull Long postId, @NotNull Pageable pageable) {
        return new PageImpl<>(
                commentRepository.findWithUserByPostId(postId, pageable),
                pageable,
                commentRepository.countByPostId(postId)
        );
    }
}
