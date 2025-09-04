package ru.ext.edu.postgres.blog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ext.edu.postgres.blog.entity.Like;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.User;
import ru.ext.edu.postgres.blog.model.ToggleLikeRequest;
import ru.ext.edu.postgres.blog.repository.LikeRepository;
import ru.ext.edu.postgres.blog.repository.PostRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private LikeRepository likeRepository;

    private LikeService likeService;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(userService, postRepository, likeRepository);
    }

    @Test
    void toggleLikeOnPost_ShouldAddLikeAndReturnTrue_WhenLikeDoesNotExist() {
        //given
        var userId = 1L;
        var postId = 2L;
        var request = new ToggleLikeRequest();
        request.setUserId(userId);

        var user = new User();
        user.setId(userId);

        var post = new Post();
        post.setId(postId);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postRepository.getReferenceById(postId)).thenReturn(post);
        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        var likeToSave = new Like();
        likeToSave.setUser(user);
        likeToSave.setPost(post);

        //when
        var result = likeService.toggleLikeOnPost(request, postId);

        //then
        assertThat(result).isTrue();
        verify(userService).getUserEntityById(userId);
        verify(postRepository).getReferenceById(postId);
        verify(likeRepository).existsByUserIdAndPostId(userId, postId);
        verify(likeRepository).save(any(Like.class));
        verify(likeRepository, never()).deleteByUserIdAndPostId(anyLong(), anyLong());
    }

    @Test
    void toggleLikeOnPost_ShouldRemoveLikeAndReturnFalse_WhenLikeExists() {
        //given
        var userId = 1L;
        var postId = 2L;
        var request = new ToggleLikeRequest();
        request.setUserId(userId);

        var user = new User();
        user.setId(userId);

        var post = new Post();
        post.setId(postId);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postRepository.getReferenceById(postId)).thenReturn(post);
        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        //when
        var result = likeService.toggleLikeOnPost(request, postId);

        //then
        assertThat(result).isFalse();
        verify(userService).getUserEntityById(userId);
        verify(postRepository).getReferenceById(postId);
        verify(likeRepository).existsByUserIdAndPostId(userId, postId);
        verify(likeRepository).deleteByUserIdAndPostId(userId, postId);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void getLikeCount_ShouldReturnCorrectCountFromRepository() {
        //given
        var postId = 1L;
        var expectedCount = 42L;
        when(likeRepository.countByPostId(postId)).thenReturn(expectedCount);

        //when
        var result = likeService.getLikeCount(postId);

        //then
        assertThat(result).isEqualTo(expectedCount);
        verify(likeRepository).countByPostId(postId);
    }
}