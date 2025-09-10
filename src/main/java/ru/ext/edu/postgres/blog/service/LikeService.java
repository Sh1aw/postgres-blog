package ru.ext.edu.postgres.blog.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ext.edu.postgres.blog.entity.Like;
import ru.ext.edu.postgres.blog.exception.EntityNotFoundException;
import ru.ext.edu.postgres.blog.repository.LikeRepository;
import ru.ext.edu.postgres.blog.repository.PostRepository;
import ru.ext.edu.postgres.blog.model.ToggleLikeRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {
    private final UserService userService;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Transactional
    @CacheEvict(value = "likeCount", key = "#postId")
    public boolean toggleLikeOnPost(@NotNull ToggleLikeRequest toggleLikeRequest, @NotNull Long postId) {
        var userId = toggleLikeRequest.getUserId();
        var user = userService.getUserEntityById(userId);

        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post", postId);
        }
        var post = postRepository.getReferenceById(postId);

        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            likeRepository.deleteByUserIdAndPostId(userId, postId);
            return false;
        } else {
            var like = new Like();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
            return true;
        }
    }

    @Cacheable(value = "likeCount", key = "#postId")
    public long getLikeCount(@NotNull Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
