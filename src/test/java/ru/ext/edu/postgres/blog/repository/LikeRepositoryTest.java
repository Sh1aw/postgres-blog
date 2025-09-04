package ru.ext.edu.postgres.blog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.ext.edu.postgres.blog.entity.Like;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

class LikeRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private Post testPost1;
    private Post testPost2;

    @BeforeEach
    void setUp() {
        testUser1 = new User()
                .setEmail("user1@example.com")
                .setLogin("user1");
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = new User()
                .setEmail("user2@example.com")
                .setLogin("user2");
        testUser2 = entityManager.persistAndFlush(testUser2);

        testPost1 = new Post()
                .setSubject("Post 1")
                .setContent("Content 1")
                .setUser(testUser1)
                .setCreatedAt(java.time.OffsetDateTime.now())
                .setActive(true);
        testPost1 = entityManager.persistAndFlush(testPost1);

        testPost2 = new Post()
                .setSubject("Post 2")
                .setContent("Content 2")
                .setUser(testUser2)
                .setCreatedAt(java.time.OffsetDateTime.now())
                .setActive(true);
        testPost2 = entityManager.persistAndFlush(testPost2);
    }

    @Test
    void existsByUserIdAndPostId_ShouldReturnTrue_WhenLikeExists() {
        //given
        var like = new Like().setUser(testUser1).setPost(testPost1);
        entityManager.persistAndFlush(like);

        //given
        boolean exists = likeRepository.existsByUserIdAndPostId(testUser1.getId(), testPost1.getId());

        //then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndPostId_ShouldReturnFalse_WhenLikeDoesNotExist() {
        //when
        boolean exists = likeRepository.existsByUserIdAndPostId(testUser1.getId(), testPost2.getId());

        //then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteByUserIdAndPostId_ShouldDeleteLike_WhenLikeExists() {
        //given
        var like = new Like()
                .setUser(testUser1)
                .setPost(testPost1);
        entityManager.persistAndFlush(like);
        assertThat(likeRepository.existsByUserIdAndPostId(testUser1.getId(), testPost1.getId())).isTrue();

        //given
        likeRepository.deleteByUserIdAndPostId(testUser1.getId(), testPost1.getId());

        //then
        assertThat(likeRepository.existsByUserIdAndPostId(testUser1.getId(), testPost1.getId())).isFalse();
    }

    @Test
    void deleteByUserIdAndPostId_ShouldDoNothing_WhenLikeDoesNotExist() {
        //given
        likeRepository.deleteByUserIdAndPostId(testUser1.getId(), testPost2.getId());

        //then
        assertThat(likeRepository.existsByUserIdAndPostId(testUser1.getId(), testPost2.getId())).isFalse();
    }

    @Test
    void countByPostId_ShouldReturnCorrectCount_WhenLikesExist() {
        //given
        Like like1 = new Like()
                .setUser(testUser1)
                .setPost(testPost1);
        entityManager.persist(like1);

        Like like2 = new Like()
                .setUser(testUser2)
                .setPost(testPost1);
        entityManager.persist(like2);

        entityManager.flush();

        //when
        long count = likeRepository.countByPostId(testPost1.getId());

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByPostId_ShouldReturnZero_WhenNoLikesExist() {
        //given

        //when
        long count = likeRepository.countByPostId(testPost1.getId());

        //then
        assertThat(count).isZero();
    }

    @Test
    void countByPostId_ShouldReturnZero_WhenPostDoesNotExist() {
        //given
        var nonExistentPostId = -1L;

        //when
        long count = likeRepository.countByPostId(nonExistentPostId);

        //then
        assertThat(count).isZero();
    }
}