package com.avesta.mastercrawler.service.social;

import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.dto.SocialPublishResponse;
import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.repository.SocialAccountRepository;
import com.avesta.mastercrawler.repository.SocialPublishJobRepository;
import com.avesta.mastercrawler.service.INewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialPublisherService {

    private final SocialAccountRepository socialAccountRepository;
    private final SocialPublishJobRepository socialPublishJobRepository;
    private final INewsService newsService;
    private final List<SocialAdapter> socialAdapters;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public SocialPublishResponse publishToSocials(SocialPublishRequest request, Users currentUser) {
        try {
            // Validate and get news
            News news = newsService.findById(request.getNewsId())
                    .orElseThrow(() -> new IllegalArgumentException("News not found"));

            // Get available social accounts for requested platforms
            List<Long> jobIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (SocialAccount.SocialPlatform platform : request.getPlatforms()) {
                try {
                    List<SocialAccount> accounts = socialAccountRepository
                            .findAvailableAccountsForUserAndPlatform(currentUser, platform);
                    
                    if (accounts.isEmpty()) {
                        errors.add("No configured account found for platform: " + platform);
                        continue;
                    }

                    // Use the first available account (could be enhanced to let user choose)
                    SocialAccount account = accounts.get(0);

                    // Create job
                    SocialPublishJob job = SocialPublishJob.builder()
                            .news(news)
                            .socialAccount(account)
                            .status(SocialPublishJob.JobStatus.QUEUED)
                            .scheduledAt(request.getScheduledAt())
                            .postData(objectMapper.writeValueAsString(request))
                            .build();

                    job = socialPublishJobRepository.save(job);
                    jobIds.add(job.getId());

                    log.info("Created social publish job {} for platform {} and news {}", 
                            job.getId(), platform, news.getId());

                } catch (Exception e) {
                    log.error("Failed to create job for platform {}", platform, e);
                    errors.add("Failed to create job for " + platform + ": " + e.getMessage());
                }
            }

            // Process jobs immediately if not scheduled
            if (request.getScheduledAt() == null) {
                processQueuedJobs();
            }

            String message = "Jobs created: " + jobIds.size();
            if (!errors.isEmpty()) {
                message += ". Errors: " + String.join(", ", errors);
            }

            return SocialPublishResponse.builder()
                    .jobIds(jobIds)
                    .message(message)
                    .success(!jobIds.isEmpty())
                    .build();

        } catch (Exception e) {
            log.error("Error publishing to socials", e);
            return SocialPublishResponse.builder()
                    .jobIds(Collections.emptyList())
                    .message("Error: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public SocialPublishResponse.JobStatus getJobStatus(Long jobId) {
        Optional<SocialPublishJob> jobOpt = socialPublishJobRepository.findById(jobId);
        
        if (jobOpt.isEmpty()) {
            return SocialPublishResponse.JobStatus.builder()
                    .jobId(jobId)
                    .status(SocialPublishJob.JobStatus.FAILED)
                    .errorMessage("Job not found")
                    .build();
        }

        SocialPublishJob job = jobOpt.get();
        // Force loading of lazy associations
        String platform = job.getSocialAccount() != null ? job.getSocialAccount().getPlatform().toString() : "UNKNOWN";
        
        return SocialPublishResponse.JobStatus.builder()
                .jobId(job.getId())
                .status(job.getStatus())
                .platform(platform)
                .errorMessage(job.getErrorMessage())
                .platformPostId(job.getPlatformPostId())
                .executedAt(job.getExecutedAt())
                .retryCount(job.getRetryCount())
                .build();
    }

    @Async
    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    public void processQueuedJobs() {
        List<SocialPublishJob> queuedJobs = socialPublishJobRepository
                .findJobsToProcess(SocialPublishJob.JobStatus.QUEUED, LocalDateTime.now());

        for (SocialPublishJob job : queuedJobs) {
            processJob(job);
        }

        // Also retry failed jobs
        List<SocialPublishJob> failedJobs = socialPublishJobRepository.findJobsToRetry();
        for (SocialPublishJob job : failedJobs) {
            processJob(job);
        }
    }

        @Transactional
        public void processJob(SocialPublishJob job) {
            try {
                job.setStatus(SocialPublishJob.JobStatus.PROCESSING);
                job = socialPublishJobRepository.save(job);

                // Reload the job with all necessary associations eagerly to avoid LazyInitializationException
                job = socialPublishJobRepository.findByIdWithAssociations(job.getId()).orElseThrow();
                
                // Force initialization of the social account to avoid lazy loading issues
                SocialAccount socialAccount = job.getSocialAccount();
                if (socialAccount == null) {
                    failJob(job, "Social account not found for job");
                    return;
                }
                
                // Get platform safely - the association should be loaded now
                SocialAccount.SocialPlatform platform;
                try {
                    platform = socialAccount.getPlatform();
                    log.info("Processing job {} for platform {} using account {}", job.getId(), platform, socialAccount.getAccountName());
                } catch (Exception e) {
                    log.error("Failed to get platform from social account", e);
                    failJob(job, "Failed to access social account: " + e.getMessage());
                    return;
                }
            
            // Find the appropriate adapter
            SocialAdapter adapter = findAdapter(platform);
            if (adapter == null) {
                failJob(job, "No adapter found for platform: " + platform);
                return;
            }

            // Parse the post data
            SocialPublishRequest request = objectMapper.readValue(job.getPostData(), SocialPublishRequest.class);
            
            // Enhance request with news data
            enhanceRequestWithNewsData(request, job.getNews());

            // Validate account
            if (!adapter.validateAccount(socialAccount)) {
                failJob(job, "Account validation failed - credentials may be expired");
                return;
            }

            // Post to platform
            String platformPostId = adapter.post(request, socialAccount);

            // Mark as successful
            job.setStatus(SocialPublishJob.JobStatus.SUCCESS);
            job.setPlatformPostId(platformPostId);
            job.setExecutedAt(LocalDateTime.now());
            job.setErrorMessage(null);
            socialPublishJobRepository.save(job);

            log.info("Successfully processed job {} for platform {}", 
                    job.getId(), job.getSocialAccount().getPlatform());

        } catch (SocialPublishException e) {
            log.error("Social publish exception for job {}", job.getId(), e);
            
            if (e.isRetryable() && job.canRetry()) {
                job.incrementRetry();
                job.setStatus(SocialPublishJob.JobStatus.QUEUED);
                job.setErrorMessage(e.getMessage());
                log.info("Retrying job {} (attempt {})", job.getId(), job.getRetryCount());
            } else {
                failJob(job, e.getMessage());
            }
            socialPublishJobRepository.save(job);

        } catch (Exception e) {
            log.error("Unexpected error processing job {}", job.getId(), e);
            
            if (job.canRetry()) {
                job.incrementRetry();
                job.setStatus(SocialPublishJob.JobStatus.QUEUED);
                job.setErrorMessage("Unexpected error: " + e.getMessage());
            } else {
                failJob(job, "Unexpected error: " + e.getMessage());
            }
            socialPublishJobRepository.save(job);
        }
    }

    private void failJob(SocialPublishJob job, String errorMessage) {
        job.setStatus(SocialPublishJob.JobStatus.FAILED);
        job.setErrorMessage(errorMessage);
        job.setExecutedAt(LocalDateTime.now());
        log.error("Job {} failed: {}", job.getId(), errorMessage);
    }

    private SocialAdapter findAdapter(SocialAccount.SocialPlatform platform) {
        return socialAdapters.stream()
                .filter(adapter -> adapter.getSupportedPlatform() == platform)
                .findFirst()
                .orElse(null);
    }

    private void enhanceRequestWithNewsData(SocialPublishRequest request, News news) {
        // Fill in any missing data from the news entity
        if (request.getTitle() == null) {
            request.setTitle(news.getTitle());
        }
        if (request.getLead() == null) {
            request.setLead(news.getLead());
        }
        if (request.getContent() == null) {
            request.setContent(news.getBody());
        }
        if (request.getImageUrl() == null) {
            request.setImageUrl(news.getMainImage());
        }
        if (request.getMetaTitle() == null) {
            request.setMetaTitle(news.getMetaTitle());
        }
        if (request.getMetaDescription() == null) {
            request.setMetaDescription(news.getMetaDescription());
        }
        if (request.getMetaKeywords() == null) {
            request.setMetaKeywords(news.getMetaKeywords());
        }
        if (request.getTags() == null && news.getTags() != null) {
            request.setTags(news.getTags().stream()
                    .map(Tags::getName)
                    .collect(Collectors.toList()));
        }
        if (request.getNewsUrl() == null) {
            // Generate news URL - this would depend on your URL structure
            request.setNewsUrl("https://yoursite.com/news/" + news.getId());
        }
    }

    public List<SocialPublishResponse.JobStatus> getJobStatusesByNews(Integer newsId) {
        Optional<News> newsOpt = newsService.findById(newsId);
        if (newsOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<SocialPublishJob> jobs = socialPublishJobRepository.findByNewsOrderByCreatedAtDesc(newsOpt.get());
        
        return jobs.stream()
                .map(job -> SocialPublishResponse.JobStatus.builder()
                        .jobId(job.getId())
                        .status(job.getStatus())
                        .platform(job.getSocialAccount().getPlatform().toString())
                        .errorMessage(job.getErrorMessage())
                        .platformPostId(job.getPlatformPostId())
                        .executedAt(job.getExecutedAt())
                        .retryCount(job.getRetryCount())
                        .build())
                .collect(Collectors.toList());
    }
}
