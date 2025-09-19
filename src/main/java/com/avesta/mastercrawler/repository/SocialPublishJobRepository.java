package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.SocialPublishJob;
import com.avesta.mastercrawler.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialPublishJobRepository extends JpaRepository<SocialPublishJob, Long> {

    List<SocialPublishJob> findByNewsAndStatus(News news, SocialPublishJob.JobStatus status);

    List<SocialPublishJob> findByStatusOrderByCreatedAtAsc(SocialPublishJob.JobStatus status);

    @Query("SELECT spj FROM SocialPublishJob spj LEFT JOIN FETCH spj.socialAccount sa LEFT JOIN FETCH spj.news WHERE spj.status = :status AND (spj.scheduledAt IS NULL OR spj.scheduledAt <= :now) ORDER BY spj.createdAt ASC")
    List<SocialPublishJob> findJobsToProcess(@Param("status") SocialPublishJob.JobStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT spj FROM SocialPublishJob spj LEFT JOIN FETCH spj.socialAccount sa LEFT JOIN FETCH spj.news WHERE spj.status = 'FAILED' AND spj.retryCount < spj.maxRetries ORDER BY spj.createdAt ASC")
    List<SocialPublishJob> findJobsToRetry();

    @Query("SELECT spj FROM SocialPublishJob spj LEFT JOIN FETCH spj.socialAccount WHERE spj.news = :news ORDER BY spj.createdAt DESC")
    List<SocialPublishJob> findByNewsOrderByCreatedAtDesc(@Param("news") News news);

    @Query("SELECT spj FROM SocialPublishJob spj LEFT JOIN FETCH spj.socialAccount sa LEFT JOIN FETCH spj.news WHERE spj.id = :id")
    Optional<SocialPublishJob> findByIdWithAssociations(@Param("id") Long id);
}
