package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Images;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagesRepository extends JpaRepository<Images, Integer> {
    List<Images> findAllByImageName(String name);
    Page<Images> findAllByImageNameContaining(String imageName, Pageable pageable);
}
