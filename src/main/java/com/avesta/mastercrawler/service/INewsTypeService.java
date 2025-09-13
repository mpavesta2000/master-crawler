package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.NewsType;

import java.util.List;
import java.util.Optional;

public interface INewsTypeService {
    List<NewsType> findAll();
    Optional<NewsType> findById(int i);
}
