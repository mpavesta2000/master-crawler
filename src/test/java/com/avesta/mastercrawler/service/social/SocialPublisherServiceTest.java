package com.avesta.mastercrawler.service.social;

import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.dto.SocialPublishResponse;
import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.repository.SocialAccountRepository;
import com.avesta.mastercrawler.repository.SocialPublishJobRepository;
import com.avesta.mastercrawler.service.INewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class SocialPublisherServiceTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private SocialPublishJobRepository socialPublishJobRepository;

    @Mock
    private INewsService newsService;

    @Mock
    private List<SocialAdapter> socialAdapters;

    @InjectMocks
    private SocialPublisherService socialPublisherService;

    private Users testUser;
    private News testNews;
    private SocialAccount testAccount;
    private SocialPublishRequest testRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testUser = new Users();
        testUser.setEmail("test@test.com");

        testNews = new News();
        testNews.setId(1);
        testNews.setTitle("Test News");
        testNews.setLead("Test Lead");
        testNews.setBody("Test Body");

        testAccount = SocialAccount.builder()
                .id(1L)
                .user(testUser)
                .platform(SocialAccount.SocialPlatform.X)
                .accessToken("encrypted_token")
                .isActive(true)
                .isGlobal(false)
                .build();

        testRequest = new SocialPublishRequest();
        testRequest.setNewsId(1);
        testRequest.setTitle("Test News");
        testRequest.setLead("Test Lead");
        testRequest.setPlatforms(Arrays.asList(SocialAccount.SocialPlatform.X));
    }

    @Test
    void testPublishToSocials_Success() {
        // Given
        when(newsService.findById(anyInt())).thenReturn(Optional.of(testNews));
        when(socialAccountRepository.findAvailableAccountsForUserAndPlatform(any(), any()))
                .thenReturn(Arrays.asList(testAccount));
        when(socialPublishJobRepository.save(any(SocialPublishJob.class)))
                .thenAnswer(invocation -> {
                    SocialPublishJob job = invocation.getArgument(0);
                    job.setId(1L);
                    return job;
                });

        // When
        SocialPublishResponse response = socialPublisherService.publishToSocials(testRequest, testUser);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(1, response.getJobIds().size());
        assertEquals(Long.valueOf(1), response.getJobIds().get(0));
        verify(socialPublishJobRepository, times(1)).save(any(SocialPublishJob.class));
    }

    @Test
    void testPublishToSocials_NewsNotFound() {
        // Given
        when(newsService.findById(anyInt())).thenReturn(Optional.empty());

        // When
        SocialPublishResponse response = socialPublisherService.publishToSocials(testRequest, testUser);

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("News not found"));
        assertEquals(0, response.getJobIds().size());
        verify(socialPublishJobRepository, never()).save(any());
    }

    @Test
    void testPublishToSocials_NoAccountFound() {
        // Given
        when(newsService.findById(anyInt())).thenReturn(Optional.of(testNews));
        when(socialAccountRepository.findAvailableAccountsForUserAndPlatform(any(), any()))
                .thenReturn(Arrays.asList()); // Empty list

        // When
        SocialPublishResponse response = socialPublisherService.publishToSocials(testRequest, testUser);

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("No configured account found"));
        assertEquals(0, response.getJobIds().size());
        verify(socialPublishJobRepository, never()).save(any());
    }

    @Test
    void testGetJobStatus_Success() {
        // Given
        SocialPublishJob job = SocialPublishJob.builder()
                .id(1L)
                .status(SocialPublishJob.JobStatus.SUCCESS)
                .socialAccount(testAccount)
                .platformPostId("post123")
                .retryCount(0)
                .build();

        when(socialPublishJobRepository.findById(1L)).thenReturn(Optional.of(job));

        // When
        SocialPublishResponse.JobStatus status = socialPublisherService.getJobStatus(1L);

        // Then
        assertEquals(Long.valueOf(1), status.getJobId());
        assertEquals(SocialPublishJob.JobStatus.SUCCESS, status.getStatus());
        assertEquals("X", status.getPlatform());
        assertEquals("post123", status.getPlatformPostId());
        assertEquals(0, status.getRetryCount());
    }

    @Test
    void testGetJobStatus_JobNotFound() {
        // Given
        when(socialPublishJobRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        SocialPublishResponse.JobStatus status = socialPublisherService.getJobStatus(1L);

        // Then
        assertEquals(Long.valueOf(1), status.getJobId());
        assertEquals(SocialPublishJob.JobStatus.FAILED, status.getStatus());
        assertEquals("Job not found", status.getErrorMessage());
    }
}
