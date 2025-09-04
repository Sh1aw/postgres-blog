package ru.ext.edu.postgres.blog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.Tag;
import ru.ext.edu.postgres.blog.entity.User;
import ru.ext.edu.postgres.blog.exception.UnauthorizedAccessException;
import ru.ext.edu.postgres.blog.mapper.PostMapper;
import ru.ext.edu.postgres.blog.model.*;
import ru.ext.edu.postgres.blog.repository.PostRepository;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserService userService;
    @Mock
    private TagService tagService;
    @Mock
    private PostMapper postMapper;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userService, tagService, postMapper);
    }

    @Test
    void createPost_ShouldSavePostAndReturnId_WhenValidRequest() {
        //given
        var userId = 1L;
        var user = new User();
        user.setId(userId);
        user.setLogin("testuser");
        user.setEmail("test@example.com");

        var tagTitles = Set.of("Tech", "Java");
        var tags = tagTitles.stream().map(title -> {
            var tag = new Tag();
            tag.setName(title);
            return tag;
        }).toList();

        var request = new CreatePostRequest();
        request.setUserId(userId);
        request.setSubject("Test Subject");
        request.setText("Test Content");
        request.setTags(tagTitles.stream().map(title -> {
            var dto = new CreatePostTagDto();
            dto.setTitle(title);
            return dto;
        }).toList());

        var postToSave = new Post();
        postToSave.setSubject(request.getSubject());
        postToSave.setContent(request.getText());
        postToSave.setUser(user);
        postToSave.setTags(Set.copyOf(tags));
        postToSave.setCreatedAt(OffsetDateTime.now());
        postToSave.setActive(true);

        var savedPost = new Post();
        savedPost.setId(10L);
        savedPost.setSubject(request.getSubject());
        savedPost.setContent(request.getText());
        savedPost.setUser(user);
        savedPost.setTags(Set.copyOf(tags));
        savedPost.setCreatedAt(postToSave.getCreatedAt());
        savedPost.setActive(postToSave.isActive());

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(tagService.batchProcessTagName(tagTitles)).thenReturn(new HashSet<>(tags));
        when(postMapper.toPostEntity(request)).thenReturn(postToSave);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        //when
        var resultId = postService.createPost(request);

        //then
        assertThat(resultId).isEqualTo(savedPost.getId());
        verify(userService).getUserEntityById(userId);
        verify(tagService).batchProcessTagName(tagTitles);
        verify(postMapper).toPostEntity(request);
        verify(postRepository).save(postToSave);
    }

    @Test
    void updatePost_ShouldThrowException_WhenUserIdsDoNotMatch() {
        //given
        var postId = 1L;
        var requestUserId = 2L;
        var postOwnerId = 3L;

        var existingPost = new Post();
        existingPost.setId(postId);
        var owner = new User();
        owner.setId(postOwnerId);
        existingPost.setUser(owner);

        var request = new UpdatePostRequest();
        request.setUserId(requestUserId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        assertThatThrownBy(() -> postService.updatePost(request, postId))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(postRepository).findById(postId);
        verifyNoInteractions(tagService);
        verifyNoInteractions(postMapper);
        verify(postRepository, never()).save(any());
    }

    @Test
    void hidePost_ShouldSetIsActiveToFalseAndSave_WhenPostExists() {
        //given
        var postId = 1L;
        var existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setActive(true);

        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        var deactivatedPost = new Post();
        deactivatedPost.setId(postId);
        deactivatedPost.setActive(false);
        when(postRepository.save(any(Post.class))).thenReturn(deactivatedPost);

        //when
        postService.hidePost(postId);

        //then
        assertThat(existingPost.isActive()).isFalse();
        verify(postRepository).findById(postId);
        verify(postRepository).save(existingPost);
    }

    @Test
    void getPostEntityById_ShouldReturnPost_WhenPostExists() {
        //given
        var postId = 1L;
        var expectedPost = new Post();
        expectedPost.setId(postId);
        expectedPost.setSubject("Test Post");

        when(postRepository.findById(postId)).thenReturn(Optional.of(expectedPost));

        //when
        var result = postService.getPostEntityById(postId);

        //then
        assertThat(result).isSameAs(expectedPost);
        verify(postRepository).findById(postId);
    }

    @Test
    void getPostEntityById_ShouldThrowException_WhenPostDoesNotExist() {
        //given
        var postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> postService.getPostEntityById(postId))
                .isInstanceOf(RuntimeException.class);

        verify(postRepository).findById(postId);
    }

    @Test
    void searchPosts_ShouldCallSearchByQuery_WhenQueryIsProvided() {
        //given
        var pageable = PageRequest.of(0, 10);
        var query = "search term";
        var tagName = (String) null;

        var post1 = new Post();
        post1.setId(1L);
        var post2 = new Post();
        post2.setId(2L);
        var posts = new PageImpl<>(List.of(post1, post2), pageable, 2);

        var postDto1 = new PostDto().id(1L).subject("Post 1");
        var postDto2 = new PostDto().id(2L).subject("Post 2");

        when(postRepository.searchByQueryRussian(query, pageable)).thenReturn(posts);
        when(postMapper.toPostDto(post1)).thenReturn(postDto1);
        when(postMapper.toPostDto(post2)).thenReturn(postDto2);

        //when
        var result = postService.searchPosts(pageable, tagName, query);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(postDto1, postDto2);
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(2);
        verify(postRepository).searchByQueryRussian(query, pageable);
        verify(postMapper).toPostDto(post1);
        verify(postMapper).toPostDto(post2);
        verify(postRepository, never()).findByTagName(anyString(), any());
        verify(postRepository, never()).findByIsActiveTrue(any());
    }

    @Test
    void searchPosts_ShouldCallFindByTagName_WhenTagNameIsProvided() {
        //given
        var pageable = PageRequest.of(1, 5);
        var tagName = "Tech";
        var query = (String) null;

        var post1 = new Post();
        post1.setId(1L);
        var posts = new PageImpl<>(List.of(post1), pageable, 1);

        var postDto1 = new PostDto().id(1L).subject("Tech Post");

        when(postRepository.findByTagName(tagName, pageable)).thenReturn(posts);
        when(postMapper.toPostDto(post1)).thenReturn(postDto1);

        //when
        var result = postService.searchPosts(pageable, tagName, query);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(postDto1);
        verify(postRepository).findByTagName(tagName, pageable);
        verify(postMapper).toPostDto(post1);
        verify(postRepository, never()).searchByQueryRussian(anyString(), any());
        verify(postRepository, never()).findByIsActiveTrue(any());
    }

    @Test
    void searchPosts_ShouldCallFindByIsActiveTrue_WhenNeitherQueryNorTagNameProvided() {
        //given
        var pageable = PageRequest.of(0, 20);
        var tagName = (String) null;
        var query = (String) null;

        var post1 = new Post();
        post1.setId(1L);
        var post2 = new Post();
        post2.setId(2L);
        var post3 = new Post();
        post3.setId(3L);
        var posts = new PageImpl<>(List.of(post1, post2, post3), pageable, 3);

        var postDto1 = new PostDto().id(1L).subject("Post 1");
        var postDto2 = new PostDto().id(2L).subject("Post 2");
        var postDto3 = new PostDto().id(3L).subject("Post 3");

        when(postRepository.findByIsActiveTrue(pageable)).thenReturn(posts);
        when(postMapper.toPostDto(post1)).thenReturn(postDto1);
        when(postMapper.toPostDto(post2)).thenReturn(postDto2);
        when(postMapper.toPostDto(post3)).thenReturn(postDto3);

        //when
        var result = postService.searchPosts(pageable, tagName, query);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(postDto1, postDto2, postDto3);
        verify(postRepository).findByIsActiveTrue(pageable);
        verify(postMapper).toPostDto(post1);
        verify(postMapper).toPostDto(post2);
        verify(postMapper).toPostDto(post3);
        verify(postRepository, never()).searchByQueryRussian(anyString(), any());
        verify(postRepository, never()).findByTagName(anyString(), any());
    }
}