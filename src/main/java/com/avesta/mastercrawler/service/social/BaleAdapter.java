package com.avesta.mastercrawler.service.social;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.utility.CryptoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaleAdapter implements SocialAdapter {

    private final CryptoUtil cryptoUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Bale uses Telegram-like API structure
    private static final String BALE_API_BASE = "https://tapi.bale.ai/bot";
    
    @Override
    public String post(SocialPublishRequest request, SocialAccount account) throws SocialPublishException {
        try {
            String botToken = cryptoUtil.decrypt(account.getAccessToken());
            String chatId = getChatIdFromSettings(account);
            
            if (chatId == null) {
                throw new SocialPublishException("Chat ID not configured for Bale account", "BALE", 400, false);
            }
            
            String content = formatContent(request);
            
            // Choose method based on whether we have an image
            if (request.getSettings() != null && request.getSettings().isIncludeImage() 
                && request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                return sendPhoto(botToken, chatId, content, request.getImageUrl());
            } else {
                return sendMessage(botToken, chatId, content);
            }
            
        } catch (Exception e) {
            log.error("Error posting to Bale", e);
            throw new SocialPublishException("Failed to post to Bale: " + e.getMessage(), "BALE", e);
        }
    }
    
    private String sendMessage(String botToken, String chatId, String text) throws SocialPublishException {
        try {
            String url = BALE_API_BASE + botToken + "/sendMessage";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("chat_id", chatId);
            params.add("text", text);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            if (responseJson.path("ok").asBoolean()) {
                String messageId = responseJson.path("result").path("message_id").asText();
                log.info("Successfully sent Bale message with ID: {}", messageId);
                return messageId;
            } else {
                String errorDescription = responseJson.path("description").asText();
                throw new SocialPublishException("Bale API error: " + errorDescription, "BALE", 400, false);
            }
            
        } catch (HttpClientErrorException e) {
            handleHttpError(e);
            throw new SocialPublishException("Failed to send Bale message", "BALE", e);
        } catch (Exception e) {
            throw new SocialPublishException("Failed to send Bale message: " + e.getMessage(), "BALE", e);
        }
    }
    
    private String sendPhoto(String botToken, String chatId, String caption, String imageUrl) throws SocialPublishException {
        try {
            String url = BALE_API_BASE + botToken + "/sendPhoto";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("chat_id", chatId);
            params.add("photo", imageUrl);
            params.add("caption", caption);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            if (responseJson.path("ok").asBoolean()) {
                String messageId = responseJson.path("result").path("message_id").asText();
                log.info("Successfully sent Bale photo with ID: {}", messageId);
                return messageId;
            } else {
                String errorDescription = responseJson.path("description").asText();
                throw new SocialPublishException("Bale API error: " + errorDescription, "BALE", 400, false);
            }
            
        } catch (Exception e) {
            log.warn("Failed to send photo, falling back to text message", e);
            // Fallback to text message
            return sendMessage(botToken, chatId, caption);
        }
    }
    
    private String getChatIdFromSettings(SocialAccount account) {
        try {
            if (account.getSettings() != null) {
                JsonNode settings = objectMapper.readTree(account.getSettings());
                return settings.path("chatId").asText(null);
            }
        } catch (Exception e) {
            log.warn("Failed to parse Bale settings", e);
        }
        return null;
    }
    
    private void handleHttpError(HttpClientErrorException e) throws SocialPublishException {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        
        if (status == HttpStatus.UNAUTHORIZED) {
            throw new SocialPublishException("Bot token is invalid", "BALE", 401, false, e);
        }
        
        if (status == HttpStatus.BAD_REQUEST) {
            throw new SocialPublishException("Bad request: " + responseBody, "BALE", 400, false, e);
        }
        
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            throw new SocialPublishException("Rate limit exceeded", "BALE", 429, true, e);
        }
        
        // Server errors are retryable
        if (status.is5xxServerError()) {
            throw new SocialPublishException("Server error: " + responseBody, "BALE", status.value(), true, e);
        }
    }
    
    @Override
    public boolean validateAccount(SocialAccount account) {
        try {
            String botToken = cryptoUtil.decrypt(account.getAccessToken());
            String url = BALE_API_BASE + botToken + "/getMe";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            return responseJson.path("ok").asBoolean();
        } catch (Exception e) {
            log.warn("Failed to validate Bale account: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public SocialAccount.SocialPlatform getSupportedPlatform() {
        return SocialAccount.SocialPlatform.BALE;
    }
    
    @Override
    public String formatContent(SocialPublishRequest request) {
        StringBuilder content = new StringBuilder();
        
        // Add custom message if provided
        if (request.getSettings() != null && request.getSettings().getCustomMessage() != null) {
            content.append(request.getSettings().getCustomMessage()).append("\n\n");
        }
        
        // Add title with Bale emoji
        if (request.getTitle() != null) {
            content.append("📢 ").append(request.getTitle());
        }
        
        // Add lead/summary
        if (request.getLead() != null && !request.getLead().trim().isEmpty()) {
            content.append("\n\n").append(request.getLead());
        }
        
        // Add URL if enabled
        if (request.getSettings() != null && request.getSettings().isIncludeUrl() 
            && request.getNewsUrl() != null) {
            content.append("\n\n").append("🔗 ادامه مطلب: ").append(request.getNewsUrl());
        }
        
        // Bale has similar limits to Telegram
        if (content.length() > 4096) {
            String text = content.toString();
            content = new StringBuilder(text.substring(0, 4090) + "...");
        }
        
        return content.toString();
    }
}
