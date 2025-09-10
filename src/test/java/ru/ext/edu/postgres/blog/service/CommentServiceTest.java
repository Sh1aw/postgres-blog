package ru.ext.edu.postgres.blog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.ext.edu.postgres.blog.entity.Comment;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.User;
import ru.ext.edu.postgres.blog.mapper.CommentMapper;
import ru.ext.edu.postgres.blog.model.*;
import ru.ext.edu.postgres.blog.repository.CommentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private PostService postService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(userService, postService, commentRepository, commentMapper);
    }

    @Test
    void addComment_ShouldSaveCommentAndReturnId_WhenValidRequest() {
        //given
        var postId = 1L;
        var authorId = 2L;
        var content = "Test comment content";

        var post = new Post();
        post.setId(postId);

        var author = new User();
        author.setId(authorId);

        var request = new CreateCommentRequest();
        request.setPostId(postId);
        request.setAuthorId(authorId);
        request.setContent(content);

        var commentToSave = new Comment();
        commentToSave.setPost(post);
        commentToSave.setUser(author);
        commentToSave.setContent(content);

        var savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setPost(post);
        savedComment.setUser(author);
        savedComment.setContent(content);
        savedComment.setCreatedAt(commentToSave.getCreatedAt());
        savedComment.setActive(true);

        when(postService.getPostEntityById(postId)).thenReturn(post);
        when(userService.getUserEntityById(authorId)).thenReturn(author);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        //when
        var resultId = commentService.addComment(request);

        //then
        assertThat(resultId).isEqualTo(savedComment.getId());
        verify(postService).getPostEntityById(postId);
        verify(userService).getUserEntityById(authorId);
        verify(commentRepository).save(argThat(c -> c.getContent().equals(content)));
    }

    @Test
    void updateComment_ShouldUpdateCommentAndReturnId_WhenValidRequestAndUserIsAuthor() {
        //given
        var commentId = 1L;
        var authorId = 2L;
        var newContent = "Updated comment content";

        var existingComment = new Comment();
        existingComment.setId(commentId);
        var author = new User();
        author.setId(authorId);
        existingComment.setUser(author);
        existingComment.setContent("Old content");

        var request = new UpdateCommentRequest();
        request.setAuthorId(authorId);
        request.setContent(newContent);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(userService.isUserExist(authorId)).thenReturn(true);

        var updatedComment = new Comment();
        updatedComment.setId(commentId);
        updatedComment.setUser(author);
        updatedComment.setContent(newContent);
        updatedComment.setCreatedAt(existingComment.getCreatedAt());
        updatedComment.setActive(existingComment.isActive());

        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

        //when
        var resultId = commentService.updateComment(request, commentId);

        //then
        assertThat(resultId).isEqualTo(commentId);
        assertThat(existingComment.getContent()).isEqualTo(newContent);
        verify(commentRepository).findById(commentId);
        verify(userService).isUserExist(authorId);
        verify(commentRepository).save(existingComment);
    }

    @Test
    void updateComment_ShouldThrowException_WhenUserIsNotAuthor() {
        //given
        var commentId = 1L;
        var authorId = 2L;
        var requesterId = 3L;

        var existingComment = new Comment();
        existingComment.setId(commentId);
        var actualAuthor = new User();
        actualAuthor.setId(authorId);
        existingComment.setUser(actualAuthor);

        var request = new UpdateCommentRequest();
        request.setAuthorId(requesterId);
        request.setContent("Some content");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        assertThatThrownBy(() -> commentService.updateComment(request, commentId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(commentRepository).findById(commentId);
        verifyNoInteractions(userService);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void hideComment_ShouldSetIsActiveToFalseAndSave_WhenCommentExists() {
        //given
        var commentId = 1L;
        var existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setActive(true);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        var deactivatedComment = new Comment();
        deactivatedComment.setId(commentId);
        deactivatedComment.setActive(false);
        when(commentRepository.save(any(Comment.class))).thenReturn(deactivatedComment);

        //when
        commentService.hideComment(commentId);

        //then
        assertThat(existingComment.isActive()).isFalse();
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(existingComment);
    }

    @Test
    void getAllPostComments_ShouldReturnPageCommentWithMappedContent() {
        //given
        var postId = 1L;
        var pageable = PageRequest.of(0, 10);

        var comment1 = new Comment();
        comment1.setId(1L);
        comment1.setContent("Comment 1");
        var comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Comment 2");

        var commentsList = List.of(comment1, comment2);
        var totalElements = 20L;

        var commentDto1 = new CommentDto().id(1L).content("Comment 1");
        var commentDto2 = new CommentDto().id(2L).content("Comment 2");

        when(commentRepository.findWithUserByPostId(postId, pageable)).thenReturn(commentsList);
        when(commentRepository.countByPostId(postId)).thenReturn(totalElements);
        when(commentMapper.toCommentDto(comment1)).thenReturn(commentDto1);
        when(commentMapper.toCommentDto(comment2)).thenReturn(commentDto2);

        //when
        var resultPageComment = commentService.getAllPostComments(pageable, postId);

        //then
        assertThat(resultPageComment).isNotNull();
        assertThat(resultPageComment.getContent()).hasSize(2);
        assertThat(resultPageComment.getContent()).containsExactly(commentDto1, commentDto2);

        var metadata = resultPageComment.getMetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(metadata.getSize()).isEqualTo(pageable.getPageSize());
        assertThat(metadata.getTotalElements()).isEqualTo(totalElements);
        assertThat(metadata.getTotalPages()).isEqualTo((int) Math.ceil((double) totalElements / pageable.getPageSize()));

        verify(commentRepository).findWithUserByPostId(postId, pageable);
        verify(commentRepository).countByPostId(postId);
        verify(commentMapper).toCommentDto(comment1);
        verify(commentMapper).toCommentDto(comment2);
    }
}