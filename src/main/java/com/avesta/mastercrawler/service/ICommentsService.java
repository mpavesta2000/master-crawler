package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICommentsService {
    void save(Comments comment);
    Page<Comments> findAllWithFilters(String searchValue, Pageable pageable);
    void deleteById(Integer id);
    Optional<Comments> findById(Integer id);
}
