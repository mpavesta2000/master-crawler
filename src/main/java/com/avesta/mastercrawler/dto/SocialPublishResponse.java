package com.avesta.mastercrawler.dto;

import com.avesta.mastercrawler.model.SocialPublishJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPublishResponse {
    private List<Long> jobIds;
    private String message;
    private boolean success;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobStatus {
        private Long jobId;
        private SocialPublishJob.JobStatus status;
        private String platform;
        private String errorMessage;
        private String platformPostId;
        private LocalDateTime executedAt;
        private int retryCount;
    }
}
