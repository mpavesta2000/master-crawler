package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Links;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinksRepository extends JpaRepository<Links, Integer> {
}
