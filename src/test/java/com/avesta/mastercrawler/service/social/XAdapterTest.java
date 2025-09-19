package com.avesta.mastercrawler.service.social;

import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.utility.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class XAdapterTest {

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private XAdapter xAdapter;

    private SocialAccount testAccount;
    private SocialPublishRequest testRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAccount = SocialAccount.builder()
                .platform(SocialAccount.SocialPlatform.X)
                .accessToken("encrypted_token")
                .isActive(true)
                .build();

        testRequest = new SocialPublishRequest();
        testRequest.setTitle("Breaking News: Important Update");
        testRequest.setLead("This is a test lead");
        testRequest.setNewsUrl("https://example.com/news/123");
        
        SocialPublishRequest.PublishSettings settings = new SocialPublishRequest.PublishSettings();
        settings.setIncludeUrl(true);
        settings.setMaxLength(280);
        testRequest.setSettings(settings);
    }

    @Test
    void testGetSupportedPlatform() {
        assertEquals(SocialAccount.SocialPlatform.X, xAdapter.getSupportedPlatform());
    }

    @Test
    void testFormatContent_WithUrl() {
        // When
        String formattedContent = xAdapter.formatContent(testRequest);

        // Then
        assertTrue(formattedContent.contains("Breaking News: Important Update"));
        assertTrue(formattedContent.contains("https://example.com/news/123"));
        assertTrue(formattedContent.length() <= 280);
    }

    @Test
    void testFormatContent_WithCustomMessage() {
        // Given
        testRequest.getSettings().setCustomMessage("🔥 Hot News Alert!");

        // When
        String formattedContent = xAdapter.formatContent(testRequest);

        // Then
        assertTrue(formattedContent.contains("🔥 Hot News Alert!"));
        assertTrue(formattedContent.contains("Breaking News: Important Update"));
        assertTrue(formattedContent.length() <= 280);
    }

    @Test
    void testFormatContent_ExceedsLimit() {
        // Given
        String longTitle = "This is a very long news title that definitely exceeds the character limit for X posts and should be truncated properly to fit within the 280 character limit while maintaining readability and including the URL at the end";
        testRequest.setTitle(longTitle);

        // When
        String formattedContent = xAdapter.formatContent(testRequest);

        // Then
        assertTrue(formattedContent.length() <= 280);
        assertTrue(formattedContent.contains("..."));
        assertTrue(formattedContent.contains("https://example.com/news/123"));
    }

    @Test
    void testFormatContent_WithoutUrl() {
        // Given
        testRequest.getSettings().setIncludeUrl(false);

        // When
        String formattedContent = xAdapter.formatContent(testRequest);

        // Then
        assertTrue(formattedContent.contains("Breaking News: Important Update"));
        assertFalse(formattedContent.contains("https://example.com/news/123"));
    }
}
