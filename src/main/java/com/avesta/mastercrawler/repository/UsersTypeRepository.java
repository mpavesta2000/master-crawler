package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.UsersType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersTypeRepository extends JpaRepository<UsersType,Integer> {
}
