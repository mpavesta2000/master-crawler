package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagsRepository extends JpaRepository<Tags, Integer> {

    List<Tags> findByNameContainingIgnoreCase(String query);
    Optional<Tags> findByNameIgnoreCase(String tagName);
    @Query("SELECT t FROM Tags t " +
            "WHERE (:searchValue IS NULL OR :searchValue = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchValue, '%')))")
    Page<Tags> findByFilters(
            @Param("searchValue") String searchValue,
            Pageable pageable);

    @Query("SELECT t FROM Tags t " +
            "WHERE t.userId.email = :ownerEmail " +
            "AND (:searchValue IS NULL OR :searchValue = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchValue, '%')))")
    Page<Tags> findByOwnerAndFilters(
            @Param("ownerEmail") String ownerEmail,
            @Param("searchValue") String searchValue,
            Pageable pageable);
}
