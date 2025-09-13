package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Comments;
import com.avesta.mastercrawler.repository.CommentsRepository;
import com.avesta.mastercrawler.service.ICommentsService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentsServiceImpl implements ICommentsService {

    private final CommentsRepository commentsRepository;

    public void save(Comments comment) {
        commentsRepository.save(comment);
    }

    public Page<Comments> findAllWithFilters(String searchValue, Pageable pageable) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return commentsRepository.findAll(pageable);
        } else {
            return commentsRepository.findByFilters(searchValue, pageable);
        }
    }

    public void deleteById(Integer id) {
        commentsRepository.deleteById(id);
    }

    public Optional<Comments> findById(Integer id) {
        return commentsRepository.findById(id);
    }
}
