package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Category;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {

    List<Category> findAll();
    Category defaultCategory();
    Optional<Category> findById(Integer id);
    Category save(Category category);
    List<Category> findByParentIsNull();
    List<Category> findByParentIsNotNull();
    void deleteById(Integer id);
}
