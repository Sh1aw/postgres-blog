package ru.ext.edu.postgres.blog.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.ext.edu.postgres.blog.api.PostApi;
import ru.ext.edu.postgres.blog.model.CreatePostRequest;
import ru.ext.edu.postgres.blog.model.PagePost;
import ru.ext.edu.postgres.blog.model.ToggleLikeRequest;
import ru.ext.edu.postgres.blog.model.UpdatePostRequest;
import ru.ext.edu.postgres.blog.service.LikeService;
import ru.ext.edu.postgres.blog.service.PostService;

@RestController
@RequiredArgsConstructor
public class PostController implements PostApi {
    private final PostService postService;
    private final LikeService likeService;

    @Override
    public ResponseEntity<Long> createPost(CreatePostRequest createPostRequest) {
        return ResponseEntity.ok(postService.createPost(createPostRequest));
    }

    @Override
    public ResponseEntity<Long> updatePost(Long postId, UpdatePostRequest updatePostRequest) {
        return ResponseEntity.ok(postService.updatePost(updatePostRequest, postId));
    }

    @Override
    public ResponseEntity<Void> deletePost(Long postId) {
        postService.hidePost(postId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PagePost> postsGet(Pageable pageable, String tag, String query) {
        return ResponseEntity.ok(
                postService.searchPosts(pageable, tag, query)
        );
    }

    @Override
    public ResponseEntity<Boolean> toggleLike(Long postId, ToggleLikeRequest toggleLikeRequest) {
        return ResponseEntity.ok(likeService.toggleLikeOnPost(toggleLikeRequest, postId));
    }
}
