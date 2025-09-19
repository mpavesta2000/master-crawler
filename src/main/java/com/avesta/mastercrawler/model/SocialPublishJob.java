package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_publish_jobs")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialPublishJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_account_id", nullable = false)
    private SocialAccount socialAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.QUEUED;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Lob
    private String errorMessage;

    @Column(name = "post_data", columnDefinition = "TEXT")
    @Lob
    private String postData; // JSON payload sent to platform

    @Column(name = "platform_post_id")
    private String platformPostId; // Post ID returned by platform

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    public enum JobStatus {
        QUEUED, PROCESSING, SUCCESS, FAILED, CANCELLED
    }

    public boolean canRetry() {
        return retryCount < maxRetries && (status == JobStatus.FAILED || status == JobStatus.QUEUED);
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}
