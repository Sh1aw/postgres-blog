package ru.ext.edu.postgres.blog.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.ext.edu.postgres.blog.api.CommentApi;
import ru.ext.edu.postgres.blog.model.CreateCommentRequest;
import ru.ext.edu.postgres.blog.model.PageComment;
import ru.ext.edu.postgres.blog.model.UpdateCommentRequest;
import ru.ext.edu.postgres.blog.service.CommentService;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity<PageComment> commentsGet(Long postId, Pageable pageable) {
        return ResponseEntity.ok(
                commentService.getAllPostComments(pageable, postId)
        );
    }

    @Override
    public ResponseEntity<Long> createComment(CreateCommentRequest createCommentRequest) {
        return ResponseEntity.ok(
                commentService.addComment(createCommentRequest)
        );
    }

    @Override
    public ResponseEntity<Void> deleteComment(Long commentId) {
        commentService.hideComment(commentId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Long> updateComment(Long commentId, UpdateCommentRequest updateCommentRequest) {
        return ResponseEntity.ok(
                commentService.updateComment(updateCommentRequest, commentId)
        );
    }
}
