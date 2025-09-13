package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Category;
import com.avesta.mastercrawler.repository.CategoryRepository;
import com.avesta.mastercrawler.service.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category defaultCategory() {
        return categoryRepository.findByName("خبر");
    }

    @Override
    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category save(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return savedCategory;
    }

    @Override
    public List<Category> findByParentIsNull() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    public List<Category> findByParentIsNotNull() {
        return categoryRepository.findByParentIsNotNull();
    }

    @Override
    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }
}
