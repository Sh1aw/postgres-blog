package ru.ext.edu.postgres.blog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.ext.edu.postgres.blog.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User()
                .setEmail("test@example.com")
                .setLogin("testuser");

        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void findFirstByLogin_ShouldReturnUser_WhenUserExists() {
        //given
        var existingLogin = testUser.getLogin();

        //when
        var foundUserOptional = userRepository.findByLogin(existingLogin);

        //then
        assertThat(foundUserOptional).isPresent();
        var foundUser = foundUserOptional.get();
        assertThat(foundUser.getId()).isEqualTo(testUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(foundUser.getLogin()).isEqualTo(testUser.getLogin());
    }

    @Test
    void findFirstByLogin_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        //given
        var nonExistentLogin = "nonexistent";

        //when
        var foundUserOptional = userRepository.findByLogin(nonExistentLogin);

        //then
        assertThat(foundUserOptional).isEmpty();
    }


    @Test
    void findFirstByLogin_ShouldHandleEmptyStringInput() {
        //given
        var emptyLogin = "";

        //when
        var foundUserOptional = userRepository.findByLogin(emptyLogin);

        //then
        assertThat(foundUserOptional).isEmpty();
    }

    @Test
    void existsByLogin_ShouldReturnTrue_WhenUserExists() {
        //given
        var existingLogin = testUser.getLogin();

        //when
        boolean exists = userRepository.existsByLogin(existingLogin);

        //then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByLogin_ShouldReturnFalse_WhenUserDoesNotExist() {
        //given
        var nonExistentLogin = "nonexistent";

        //when
        boolean exists = userRepository.existsByLogin(nonExistentLogin);

        //then
        assertThat(exists).isFalse();
    }
}