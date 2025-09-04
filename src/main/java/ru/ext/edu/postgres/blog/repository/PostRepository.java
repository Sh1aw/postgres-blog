package ru.ext.edu.postgres.blog.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import ru.ext.edu.postgres.blog.entity.Post;

import java.time.OffsetDateTime;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph("Post.withTagsAndUser")
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.name = :tagName AND p.isActive = true")
    Page<Post> findByTagName(@Param("tagName")@NotBlank String tagName, @NonNull Pageable pageable);

    @EntityGraph("Post.withTagsAndUser")
    Page<Post> findByIsActiveTrue(Pageable pageable);

    default Page<Post> searchByQueryRussian(@NotBlank String query, @NonNull Pageable pageable) {
        return searchByQuery(query, "russian", pageable);

    }

    @Query(value = """
            SELECT p.* FROM posts p
            WHERE p.search_vector @@ plainto_tsquery(CAST(:language AS regconfig), :query)
            AND p.is_active = true
            ORDER BY ts_rank(p.search_vector, plainto_tsquery(CAST(:language AS regconfig), :query)) DESC
            """,
            countQuery = """
                    SELECT count(*) FROM posts p
                    WHERE p.search_vector @@ plainto_tsquery(CAST(:language AS regconfig), :query)
                    AND p.is_active = true
                    """,
            nativeQuery = true)
    Page<Post> searchByQuery(@Param("query") @NotBlank String query,
                             @Param("language") @NotBlank String language,
                             @NonNull Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM posts
            WHERE is_active = false
            AND created_at < :olderThan
            LIMIT :limit
            """, nativeQuery = true)
    int deleteInactivePostsOlderThanWithLimit(
            @Param("olderThan") @NotNull OffsetDateTime olderThan,
            @Param("limit") @Positive int limit
    );
}