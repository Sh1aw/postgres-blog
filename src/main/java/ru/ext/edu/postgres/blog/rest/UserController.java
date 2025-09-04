package ru.ext.edu.postgres.blog.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.ext.edu.postgres.blog.model.AuthRequest;
import ru.ext.edu.postgres.blog.model.CreateUserRequest;
import ru.ext.edu.postgres.blog.model.UserDto;
import ru.ext.edu.postgres.blog.service.UserService;
import ru.ext.edu.postgres.blog.api.AuthApi;
import ru.ext.edu.postgres.blog.api.UserApi;

@RestController("user")
@RequiredArgsConstructor
public class UserController implements AuthApi, UserApi {
    private final UserService userService;

    @Override
    public ResponseEntity<Long> login(AuthRequest authRequest) {
        return ResponseEntity.ok(
                userService.login(authRequest.getLogin())
        );
    }

    @Override
    public ResponseEntity<Long> createUser(CreateUserRequest createUserRequest) {
        return ResponseEntity.ok(
                userService.createUser(createUserRequest)
        );
    }

    @Override
    public ResponseEntity<UserDto> getUserById(Long userId) {
        return null;
    }
}
