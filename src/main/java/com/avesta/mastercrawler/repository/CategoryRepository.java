package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByParentIsNull();
    List<Category> findByParentIsNotNull();
    Category findByName(String name);
}
