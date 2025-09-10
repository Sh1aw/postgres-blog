package ru.ext.edu.postgres.blog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.User;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User()
                .setEmail("test@example.com")
                .setLogin("testuser");
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void shouldSaveAndFindPost() {
        // given
        var post = new Post()
                .setSubject("Тестовый пост")
                .setContent("Содержание тестового поста")
                .setUser(testUser)
                .setCreatedAt(OffsetDateTime.now())
                .setActive(true);

        // when
        var savedPost = postRepository.save(post);
        entityManager.flush();

        var foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getSubject()).isEqualTo("Тестовый пост");
        assertThat(foundPost.get().getUser().getId()).isEqualTo(testUser.getId());
    }
}
