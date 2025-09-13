package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Integer> {

    @Query("SELECT t FROM Comments t " +
            "WHERE (:searchValue IS NULL OR :searchValue = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchValue, '%')))")
    Page<Comments> findByFilters(
            @Param("searchValue") String searchValue,
            Pageable pageable);
}
