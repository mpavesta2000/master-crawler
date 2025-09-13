package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.AboutUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AboutUsRepository extends JpaRepository<AboutUs, Integer> {
}
