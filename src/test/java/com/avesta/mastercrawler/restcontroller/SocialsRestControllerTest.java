package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.dto.SocialPublishResponse;
import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.ISocialAccountService;
import com.avesta.mastercrawler.service.social.SocialPublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SocialsRestController.class)
class SocialsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocialPublisherService socialPublisherService;

    @MockBean
    private ISocialAccountService socialAccountService;

    @Autowired
    private ObjectMapper objectMapper;

    private SocialPublishRequest testRequest;
    private SocialPublishResponse testResponse;
    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setEmail("test@test.com");

        testRequest = new SocialPublishRequest();
        testRequest.setNewsId(1);
        testRequest.setTitle("Test News");
        testRequest.setLead("Test Lead");
        testRequest.setPlatforms(Arrays.asList(SocialAccount.SocialPlatform.X));

        testResponse = SocialPublishResponse.builder()
                .success(true)
                .message("Jobs created successfully")
                .jobIds(Arrays.asList(1L))
                .build();
    }

    @Test
    @WithMockUser
    void testPublishToSocials_Success() throws Exception {
        // Given
        when(socialPublisherService.publishToSocials(any(SocialPublishRequest.class), any(Users.class)))
                .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/api/socials/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jobs created successfully"))
                .andExpect(jsonPath("$.jobIds[0]").value(1));
    }

    @Test
    @WithMockUser
    void testPublishToSocials_InvalidRequest_NoPlatforms() throws Exception {
        // Given
        testRequest.setPlatforms(Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/api/socials/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("At least one platform must be specified"));
    }

    @Test
    @WithMockUser
    void testPublishToSocials_InvalidRequest_NoNewsId() throws Exception {
        // Given
        testRequest.setNewsId(null);

        // When & Then
        mockMvc.perform(post("/api/socials/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("News ID is required"));
    }

    @Test
    @WithMockUser
    void testGetJobStatus_Success() throws Exception {
        // Given
        SocialPublishResponse.JobStatus jobStatus = SocialPublishResponse.JobStatus.builder()
                .jobId(1L)
                .status(com.avesta.mastercrawler.model.SocialPublishJob.JobStatus.SUCCESS)
                .platform("X")
                .platformPostId("post123")
                .retryCount(0)
                .build();

        when(socialPublisherService.getJobStatus(1L)).thenReturn(jobStatus);

        // When & Then
        mockMvc.perform(get("/api/socials/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.platform").value("X"))
                .andExpect(jsonPath("$.platformPostId").value("post123"));
    }
}
