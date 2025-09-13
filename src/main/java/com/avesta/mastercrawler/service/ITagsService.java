package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ITagsService {

    Tags addTagIfNotExists(String tagName);
    Page<Tags> findAllWithFilters(String searchValue, Pageable pageable);
    Tags save(Tags tags);
    Optional<Tags> findById(Integer id);
    List<String> findTagsByQuery(String query);
    void deleteTag(Integer tagId);
}
