package ru.ext.edu.postgres.blog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ext.edu.postgres.blog.entity.Post;
import ru.ext.edu.postgres.blog.entity.Tag;
import ru.ext.edu.postgres.blog.model.CreatePostRequest;
import ru.ext.edu.postgres.blog.model.UpdatePostRequest;
import ru.ext.edu.postgres.blog.model.CreatePostTagDto;
import ru.ext.edu.postgres.blog.model.PostDto;
import ru.ext.edu.postgres.blog.service.LikeService;

@Mapper(componentModel = "spring")
public abstract class PostMapper {
    @Autowired
    protected LikeService likeService;

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "content", source = "text")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract Post toPostEntity(CreatePostRequest createPostRequest);

    @Mapping(target = "content", source = "text")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "tags", ignore = true)
    public abstract void updateFromDto(UpdatePostRequest updatePostRequest, @MappingTarget Post post);

    @Mapping(target = "text", source = "content")
    @Mapping(target = "likesCount", source = "post")
    @Mapping(target = "author", source = "user")
    public abstract PostDto toPostDto(Post post);

    protected Long mapLikeCount(Post post) {
        return likeService.getLikeCount(post.getId());
    }

    @Mapping(target = "title", source = "name")
    protected abstract CreatePostTagDto toTagDto(Tag tag);
}
