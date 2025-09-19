package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.News;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
    List<News> findByMainImage(String mainImage);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM news_tags WHERE tags_id = :tagId", nativeQuery = true)
    void removeTagFromNews(@Param("tagId") Integer tagId);

    @Query("SELECT n FROM News n " +
            "LEFT JOIN n.categories c " +
            "WHERE (:searchValue IS NULL OR :searchValue = '' OR n.title LIKE %:searchValue% OR n.userId.email LIKE %:searchValue% OR c.name LIKE %:searchValue%) " +
            "AND (:statusFilter IS NULL OR :statusFilter = '' OR n.status = :statusFilter) " +
            "AND (:newsTypeFilter IS NULL OR :newsTypeFilter = '' OR n.newsTypeId.newsTypeName = :newsTypeFilter) " +
            "AND (:userFilter IS NULL OR :userFilter = '' OR n.userId.email = :userFilter) " +
            "AND (:idFilter IS NULL OR n.id = :idFilter)")
    Page<News> findByFilters(
            @Param("searchValue") String searchValue,
            @Param("statusFilter") String statusFilter,
            @Param("newsTypeFilter") String newsTypeFilter,
            @Param("userFilter") String userFilter,
            @Param("idFilter") Integer idFilter,
            Pageable pageable);

    Page<News> findAllByGetTranslatedTrue(Pageable pageable);

    Page<News> findAllByTitleContainingAndGetTranslatedTrue(String title, Pageable pageable);

    List<News> findAllByMainVideo(String name);

    List<News> findByMainVideo(String oldVideoName);

    List<News> findByNewsHeadlineTrue();

    List<News> findByShowMostViewedTrue();

    List<News> findBySliderTrue();

    List<News> findTop10ByOrderByCreatedAtDesc();

    List<News> findTop3ByOrderByCreatedAtDesc();

    List<News> findTop2ByOrderByCreatedAtDesc();

    List<News> findByBreakingNewsTrue();

    @Modifying
    @Transactional
    @Query(value = "UPDATE news SET created_at = ?2 WHERE id = ?1", nativeQuery = true)
    void updateCreatedAtById(Integer id, LocalDateTime createdAt);


}
