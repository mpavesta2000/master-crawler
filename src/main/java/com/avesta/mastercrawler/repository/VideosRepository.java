package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Videos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideosRepository extends JpaRepository<Videos, Integer> {
    List<Videos> findAllByVideoName(String name);

    Page<Videos> findAllByVideoNameContaining(String search, Pageable pageable);
}
