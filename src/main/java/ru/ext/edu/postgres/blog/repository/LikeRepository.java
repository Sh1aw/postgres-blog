package ru.ext.edu.postgres.blog.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ext.edu.postgres.blog.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndPostId(@NotNull Long userId, @NotNull Long postId);

    void deleteByUserIdAndPostId(@NotNull Long userId, @NotNull Long postId);

    @Query("SELECT COUNT(l) FROM LikeEntity l WHERE l.post.id= :postId")
    long countByPostId(@Param("postId") @NotNull Long postId);
}