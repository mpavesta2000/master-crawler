package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.CallUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallUsRepository extends JpaRepository<CallUs, Integer> {
}
