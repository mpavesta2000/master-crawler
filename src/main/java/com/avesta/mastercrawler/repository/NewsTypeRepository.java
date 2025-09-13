package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.NewsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsTypeRepository extends JpaRepository<NewsType, Integer> {

}
