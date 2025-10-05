package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Tags;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.repository.NewsRepository;
import com.avesta.mastercrawler.repository.TagsRepository;
import com.avesta.mastercrawler.repository.UsersRepository;
import com.avesta.mastercrawler.service.ITagsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TagsServiceImpl implements ITagsService {

    private final TagsRepository tagsRepository;
    private final NewsRepository newsRepository;
    private final UsersRepository usersRepository;

    @Override
    public Tags addTagIfNotExists(String tagName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("user not found."));
        return tagsRepository.findByNameIgnoreCase(tagName)
                .orElseGet(() -> {
                    Tags newTag = new Tags();
                    newTag.setName(tagName);
                    newTag.setNews(null);
                    newTag.setUserId(user);
                    return tagsRepository.save(newTag);
                });
    }

    @Override
    public Page<Tags> findAllWithFilters(String searchValue, Pageable pageable) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return tagsRepository.findAll(pageable);
        } else {
            return tagsRepository.findByFilters(searchValue, pageable);
        }
    }

    @Override
    public Tags save(Tags tags) {
        return tagsRepository.save(tags);
    }

    @Override
    public Optional<Tags> findById(Integer id) {
        return tagsRepository.findById(id);
    }

    @Override
    public List<String> findTagsByQuery(String query) {
        return tagsRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(Tags::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTag(Integer tagId) {
        Tags tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        newsRepository.removeTagFromNews(tagId);
        tagsRepository.delete(tag);
    }


}
