package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);

    @Query("SELECT u FROM Users u " +
            "WHERE (:searchValue IS NULL OR :searchValue = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchValue, '%')))")
    Page<Users> findByFilters(
            @Param("searchValue") String searchValue,
            Pageable pageable);

    Page<Users> findAllByUserTypeId_UserTypeName(String userTypeName, Pageable pageable);
}
