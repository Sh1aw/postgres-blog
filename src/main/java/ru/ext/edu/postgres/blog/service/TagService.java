package ru.ext.edu.postgres.blog.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ext.edu.postgres.blog.entity.Tag;
import ru.ext.edu.postgres.blog.repository.TagRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public Set<Tag> batchProcessTagName(Set<String> tagTitles) {
        var normalizedTagNames = tagTitles.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        var alreadyExistTags = tagRepository.findByNameIn(normalizedTagNames);

        var alreadyExistTagsNames = alreadyExistTags.stream()
                .map(tag -> tag.getName().toLowerCase())
                .collect(Collectors.toSet());

        var newTags = normalizedTagNames.stream()
                .filter(name -> !alreadyExistTagsNames.contains(name))
                .map( tagName -> new Tag().setName(tagName))
                .collect(Collectors.toSet());
        var result = new HashSet<>(tagRepository.saveAll(newTags));
        result.addAll(alreadyExistTags);
        return result;
    }
}
