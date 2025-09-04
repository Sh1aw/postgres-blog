package ru.ext.edu.postgres.blog.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.mapper.PostMapper;
import ru.ext.edu.postgres.blog.model.PagePost;
import ru.ext.edu.postgres.blog.model.PageMetadata;
import ru.ext.edu.postgres.blog.model.CreatePostRequest;
import ru.ext.edu.postgres.blog.model.CreatePostTagDto;
import ru.ext.edu.postgres.blog.model.UpdatePostRequest;
import ru.ext.edu.postgres.blog.repository.PostRepository;

import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import ru.ext.edu.postgres.blog.exception.EntityNotFoundException;
import ru.ext.edu.postgres.blog.exception.UnauthorizedAccessException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final TagService tagService;
    private final PostMapper postMapper;

    @Transactional
    public Long createPost(CreatePostRequest post) {
        var user = userService.getUserEntityById(post.getUserId());
        var tagsToSave = tagService.batchProcessTagName(
                post.getTags().stream()
                        .map(CreatePostTagDto::getTitle)
                        .collect(Collectors.toSet())
        );
        var postToSave = postMapper.toPostEntity(post)
                .setUser(user)
                .setTags(new HashSet<>(tagsToSave));

        return postRepository.save(postToSave).getId();
    }

    @Transactional
    public Long updatePost(UpdatePostRequest updatePostRequest, Long postId) {
        var existPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post", postId));
        if (!Objects.equals(existPost.getUser().getId(), updatePostRequest.getUserId())) {
            throw new UnauthorizedAccessException("User is not authorized to update this post");
        }
        postMapper.updateFromDto(updatePostRequest, existPost);
        var tagsToSave = tagService.batchProcessTagName(
                updatePostRequest.getTags().stream()
                        .map(ru.ext.edu.postgres.blog.model.TagDto::getTitle)
                        .collect(Collectors.toSet())
        );
        existPost.setTags(new HashSet<>(tagsToSave));
        return postRepository.save(existPost).getId();
    }

    @Transactional
    public void hidePost(Long postId) {
        var existPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post", postId));
        postRepository.save(existPost.setActive(false));
    }

    @NotNull
    public Post getPostEntityById(@NotNull Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post", id));
    }

    @NotNull
    public PagePost searchPosts(@NotNull Pageable pageable, @Nullable String tagName, @Nullable String query) {
        var foundPosts = searchPostsByParams(pageable, tagName, query);

        return new PagePost().metadata(
                new PageMetadata()
                        .number(foundPosts.getNumber())
                        .totalElements(foundPosts.getTotalElements())
                        .size(foundPosts.getSize())
                        .totalPages(foundPosts.getTotalPages())
        ).content(foundPosts.stream().map(
                postMapper::toPostDto
        ).toList());
    }

    private Page<Post> searchPostsByParams(Pageable pageable, String tagName, String query) {
        if (query != null && !query.trim().isEmpty()) {
            return postRepository.searchByQueryRussian(query.trim(), pageable);
        }
        if (tagName != null && !tagName.trim().isEmpty()) {
            return postRepository.findByTagName(tagName.trim(), pageable);
        }
        return postRepository.findByIsActiveTrue(pageable);
    }
}
