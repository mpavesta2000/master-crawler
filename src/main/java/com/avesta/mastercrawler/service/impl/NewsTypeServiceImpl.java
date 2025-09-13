package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.NewsType;
import com.avesta.mastercrawler.repository.NewsTypeRepository;
import com.avesta.mastercrawler.service.INewsTypeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NewsTypeServiceImpl implements INewsTypeService {

    private final NewsTypeRepository newsTypeRepository;

    @Override
    public List<NewsType> findAll() {
        return newsTypeRepository.findAll();
    }

    @Override
    public Optional<NewsType> findById(int i) {
        return newsTypeRepository.findById(i);
    }
}
