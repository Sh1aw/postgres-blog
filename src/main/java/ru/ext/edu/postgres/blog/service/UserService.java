package ru.ext.edu.postgres.blog.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ext.edu.postgres.blog.entity.User;
import ru.ext.edu.postgres.blog.exception.EntityNotFoundException;
import ru.ext.edu.postgres.blog.mapper.UserMapper;
import ru.ext.edu.postgres.blog.model.CreateUserRequest;
import ru.ext.edu.postgres.blog.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Long login(@NotNull String login) {
        return userRepository.findByLogin(login).orElseThrow().getId();
    }

    public boolean isUserExist(@NotNull Long userId) {
        return userRepository.existsById(userId);
    }

    public User getUserEntityById(@NotNull Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    public Long createUser(@NotNull CreateUserRequest createUserRequest) {
        if (userRepository.existsByLogin(createUserRequest.getLogin())) {
            throw new IllegalArgumentException("User already exists");
        }
        return userRepository.save(
                userMapper.toUserEntity(createUserRequest)
        ).getId();
    }
}
