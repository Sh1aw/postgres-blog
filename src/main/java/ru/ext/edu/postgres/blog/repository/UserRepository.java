package ru.ext.edu.postgres.blog.repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ext.edu.postgres.blog.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(@NotBlank String login);

    boolean existsByLogin(@NotBlank String login);
}