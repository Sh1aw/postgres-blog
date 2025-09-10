package ru.ext.edu.postgres.blog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ext.edu.postgres.blog.entity.Comment;
import ru.ext.edu.postgres.blog.model.CommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {
   @Mapping(source = "user", target = "author")
   CommentDto toCommentDto(Comment comment);
}
