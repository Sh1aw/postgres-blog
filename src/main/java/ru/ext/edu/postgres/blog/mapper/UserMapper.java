package ru.ext.edu.postgres.blog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ext.edu.postgres.blog.entity.User;
import ru.ext.edu.postgres.blog.model.UserDto;
import ru.ext.edu.postgres.blog.model.CreateUserRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toUserEntity(CreateUserRequest userTo);

    UserDto toUserTo(User userTo);
}
