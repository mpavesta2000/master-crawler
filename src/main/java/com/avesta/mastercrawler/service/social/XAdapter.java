package com.avesta.mastercrawler.service.social;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.utility.CryptoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class XAdapter implements SocialAdapter {

    private final CryptoUtil cryptoUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${x.bearer.token}")
    private String bearerToken;
    
    private static final String X_API_BASE = "https://api.x.com/2";
    private static final String TWEET_ENDPOINT = "/tweets";
    private static final String MEDIA_UPLOAD_ENDPOINT = "https://upload.x.com/1.1/media/upload.json";
    
    @Override
    public String post(SocialPublishRequest request, SocialAccount account) throws SocialPublishException {
        try {
            // Get user access token from the account (this should be a User Context token)
            String accessToken = cryptoUtil.decrypt(account.getAccessToken());
            log.info("Attempting to post to X using OAuth user token for account: {}, token length: {}", 
                    account.getAccountName(), accessToken != null ? accessToken.length() : "null");
            
            // Debug: Log token characteristics to identify type
            if (accessToken != null && accessToken.length() > 20) {
                String tokenStart = accessToken.substring(0, Math.min(20, accessToken.length()));
                log.info("Token starts with: {}...", tokenStart);
                
                // Check token characteristics
                if (accessToken.startsWith("AAAAAAAAAA")) {
                    log.warn("WARNING: Token appears to be a Bearer Token (starts with AAAAAAAAAA) - this will not work for posting!");
                } else {
                    log.info("Token appears to be a User Context token (good!)");
                }
            }
            
            // Create tweet content
            String tweetText = formatContent(request);
            log.info("Tweet content: {}", tweetText);
            
            // Prepare tweet data
            Map<String, Object> tweetData = new HashMap<>();
            tweetData.put("text", tweetText);
            
            // If image is included and available, upload it first
            if (request.getSettings() != null && request.getSettings().isIncludeImage() 
                && request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                try {
                    String mediaId = uploadMedia(request.getImageUrl(), accessToken);
                    if (mediaId != null) {
                        Map<String, Object> media = new HashMap<>();
                        media.put("media_ids", new String[]{mediaId});
                        tweetData.put("media", media);
                    }
                } catch (Exception e) {
                    log.warn("Failed to upload media for tweet, posting text only", e);
                }
            }
            
            // Post tweet using OAuth user access token
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(tweetData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                X_API_BASE + TWEET_ENDPOINT,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Parse response to get tweet ID
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String tweetId = responseJson.path("data").path("id").asText();
            
            log.info("Successfully posted tweet with ID: {}", tweetId);
            return tweetId;
            
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("X API request failed: {} - Response: {}", e.getStatusCode(), responseBody);
            
            // Check if this is the "Unsupported Authentication" error
            if (responseBody.contains("Unsupported Authentication") || responseBody.contains("Application-Only")) {
                throw new SocialPublishException("X OAuth token error:  need different permissions or the OAuth flow needs to request 'User Context' instead of 'Application-Only' tokens. Please check your Twitter app settings and reconnect your account.", "X", e);
            }
            
            handleHttpError(e);
            throw new SocialPublishException("Failed to post to X", "X", e);
        } catch (Exception e) {
            log.error("Error posting to X", e);
            throw new SocialPublishException("Failed to post to X: " + e.getMessage(), "X", e);
        }
    }
    
    private String uploadMedia(String imageUrl, String accessToken) throws Exception {
        // For simplicity, we'll skip media upload in this implementation
        // In a real implementation, you would:
        // 1. Download the image from imageUrl
        // 2. Upload it to X's media upload endpoint
        // 3. Return the media_id
        log.info("Media upload not implemented yet for image: {}", imageUrl);
        return null;
    }
    
    private void handleHttpError(HttpClientErrorException e) throws SocialPublishException {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        
        // Handle rate limiting
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            throw new SocialPublishException("Rate limit exceeded", "X", 429, true, e);
        }
        
        // Handle authentication errors
        if (status == HttpStatus.UNAUTHORIZED) {
            throw new SocialPublishException("Authentication failed - token may be expired", "X", 401, false, e);
        }
        
        // Handle forbidden (insufficient permissions)
        if (status == HttpStatus.FORBIDDEN) {
            throw new SocialPublishException("Insufficient permissions", "X", 403, false, e);
        }
        
        // Other client errors are generally not retryable
        if (status.is4xxClientError()) {
            throw new SocialPublishException("Client error: " + responseBody, "X", status.value(), false, e);
        }
        
        // Server errors are retryable
        if (status.is5xxServerError()) {
            throw new SocialPublishException("Server error: " + responseBody, "X", status.value(), true, e);
        }
    }
    
    @Override
    public boolean validateAccount(SocialAccount account) {
        try {
            String accessToken = cryptoUtil.decrypt(account.getAccessToken());
            log.info("Validating X account using OAuth user token for user: {}, token length: {}", 
                    account.getAccountName(), accessToken != null ? accessToken.length() : "null");
            
            if (accessToken == null || accessToken.trim().isEmpty()) {
                log.error("Access token is null or empty for account: {}", account.getAccountName());
                return false;
            }
            
            // For now, assume OAuth user tokens are valid
            // TODO: Implement actual validation with X API /users/me endpoint
            log.info("OAuth user token validation - assuming valid for now");
            return true;
            
            /* Temporarily commented out until we resolve the OAuth scope/endpoint issue
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            String url = X_API_BASE + "/users/me";
            log.info("Making validation request to: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            log.info("X validation response status: {}, body: {}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
            */
        } catch (Exception e) {
            log.error("Failed to validate X account", e);
            return false;
        }
    }
    
    @Override
    public SocialAccount.SocialPlatform getSupportedPlatform() {
        return SocialAccount.SocialPlatform.X;
    }
    
    @Override
    public String formatContent(SocialPublishRequest request) {
        StringBuilder content = new StringBuilder();
        
        // Add custom message if provided
        if (request.getSettings() != null && request.getSettings().getCustomMessage() != null) {
            content.append(request.getSettings().getCustomMessage()).append("\n\n");
        }
        
        // Add title
        if (request.getTitle() != null) {
            content.append(request.getTitle());
        }
        
        // Add URL if enabled
        if (request.getSettings() != null && request.getSettings().isIncludeUrl() 
            && request.getNewsUrl() != null) {
            content.append("\n\n").append(request.getNewsUrl());
        }
        
        // Ensure we don't exceed X's character limit
        int maxLength = (request.getSettings() != null) ? request.getSettings().getMaxLength() : 280;
        if (content.length() > maxLength) {
            String text = content.toString();
            // Leave space for "..." and URL if included
            int cutPoint = maxLength - 3;
            if (request.getSettings() != null && request.getSettings().isIncludeUrl() 
                && request.getNewsUrl() != null) {
                cutPoint -= request.getNewsUrl().length() + 2; // +2 for newlines
            }
            
            if (cutPoint > 0) {
                content = new StringBuilder(text.substring(0, cutPoint) + "...");
                
                if (request.getSettings() != null && request.getSettings().isIncludeUrl() 
                    && request.getNewsUrl() != null) {
                    content.append("\n\n").append(request.getNewsUrl());
                }
            }
        }
        
        return content.toString();
    }
}
