package ru.ext.edu.postgres.blog.mapper;

import org.mapstruct.Mapper;
import ru.ext.edu.postgres.blog.entity.PostAudit;
import ru.ext.edu.postgres.blog.model.PostAuditDto;

@Mapper(componentModel = "spring")
public interface PostAuditMapper {
    PostAuditDto toPostAuditDto(PostAudit userTo);
}
