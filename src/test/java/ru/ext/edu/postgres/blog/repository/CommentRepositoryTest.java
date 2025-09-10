package ru.ext.edu.postgres.blog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.ext.edu.postgres.blog.entity.Comment;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.User;


import java.time.OffsetDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CommentRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Post testPost;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User()
                .setEmail("test@example.com")
                .setLogin("testuser");
        entityManager.persistAndFlush(testUser);

        testPost = new Post()
                .setSubject("Test Post")
                .setContent("Content of the test post")
                .setUser(testUser)
                .setCreatedAt(OffsetDateTime.now())
                .setActive(true);
        entityManager.persistAndFlush(testPost);

        IntStream.rangeClosed(1, 5).forEach(i -> {
            var comment = new Comment()
                    .setContent("Comment content " + i)
                    .setPost(testPost)
                    .setUser(testUser)
                    .setCreatedAt(OffsetDateTime.now().plusMinutes(i))
                    .setActive(true);
            entityManager.persist(comment);
        });
        entityManager.flush();
    }

    @Test
    void findByPostId_ShouldReturnCommentsForPost_WhenPostExists() {
        //given
        var postId = testPost.getId();
        var pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "createdAt"));

        //when
        var commentPage = commentRepository.findByPostId(postId, pageable);

        //then
        assertThat(commentPage).isNotNull();
        assertThat(commentPage.getContent()).hasSize(3);
        assertThat(commentPage.getTotalElements()).isEqualTo(5);
        assertThat(commentPage.getTotalPages()).isEqualTo(2);
        assertThat(commentPage.getNumber()).isZero();
        assertThat(commentPage.isFirst()).isTrue();
        assertThat(commentPage.hasNext()).isTrue();

        assertThat(commentPage.getContent())
                .allMatch(comment -> comment.getPost().getId().equals(postId));
    }

    @Test
    void findByPostId_ShouldReturnEmptyPage_WhenPostHasNoComments() {
        //given
        var postWithoutComments = new Post()
                .setSubject("Empty Post")
                .setContent("Content of the empty post")
                .setUser(testUser)
                .setCreatedAt(OffsetDateTime.now())
                .setActive(true);
        entityManager.persistAndFlush(postWithoutComments);

        var postId = postWithoutComments.getId();
        var pageable = PageRequest.of(0, 10);

        //when
        var commentPage = commentRepository.findByPostId(postId, pageable);

        //then
        assertThat(commentPage).isNotNull();
        assertThat(commentPage.getContent()).isEmpty();
        assertThat(commentPage.getTotalElements()).isZero();
        assertThat(commentPage.getTotalPages()).isZero();
    }

    @Test
    void findByPostId_ShouldReturnEmptyPage_WhenPostDoesNotExist() {
        //given
        var nonExistentPostId = -1L;
        var pageable = PageRequest.of(0, 10);

        //when
        var commentPage = commentRepository.findByPostId(nonExistentPostId, pageable);

        //then
        assertThat(commentPage).isNotNull();
        assertThat(commentPage.getContent()).isEmpty();
        assertThat(commentPage.getTotalElements()).isZero();
        assertThat(commentPage.getTotalPages()).isZero();
    }

    @Test
    void findByPostId_ShouldRespectPageSize() {
        //given
        var postId = testPost.getId();
        var pageableFirst = PageRequest.of(0, 2);
        var pageableSecond = PageRequest.of(1, 2);

        //when
        var firstPage = commentRepository.findByPostId(postId, pageableFirst);
        var secondPage = commentRepository.findByPostId(postId, pageableSecond);

        //then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getNumber()).isZero();
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.getNumber()).isEqualTo(1);
        assertThat(firstPage.getContent()).extracting(Comment::getId)
                .doesNotContainAnyElementsOf(secondPage.getContent().stream().map(Comment::getId).toList());
    }
}
