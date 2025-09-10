package ru.ext.edu.postgres.blog.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ext.edu.postgres.blog.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByNameIn(@NotNull Set<String> tagTittles);
}