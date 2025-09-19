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
public class EitaaAdapter implements SocialAdapter {

    private final CryptoUtil cryptoUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Eitaa uses similar API structure to Telegram
    private static final String EITAA_API_BASE = "https://eitaayar.ir/api";
    
    @Override
    public String post(SocialPublishRequest request, SocialAccount account) throws SocialPublishException {
        try {
            String botToken = cryptoUtil.decrypt(account.getAccessToken());
            String chatId = getChatIdFromSettings(account);
            
            if (chatId == null) {
                throw new SocialPublishException("Chat ID not configured for Eitaa account", "EITAA", 400, false);
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
            log.error("Error posting to Eitaa", e);
            throw new SocialPublishException("Failed to post to Eitaa: " + e.getMessage(), "EITAA", e);
        }
    }
    
    private String sendMessage(String botToken, String chatId, String text) throws SocialPublishException {
        try {
            String url = EITAA_API_BASE + "/" + botToken + "/sendMessage";
            
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
                log.info("Successfully sent Eitaa message with ID: {}", messageId);
                return messageId;
            } else {
                String errorDescription = responseJson.path("description").asText();
                throw new SocialPublishException("Eitaa API error: " + errorDescription, "EITAA", 400, false);
            }
            
        } catch (HttpClientErrorException e) {
            handleHttpError(e);
            throw new SocialPublishException("Failed to send Eitaa message", "EITAA", e);
        } catch (Exception e) {
            throw new SocialPublishException("Failed to send Eitaa message: " + e.getMessage(), "EITAA", e);
        }
    }
    
    private String sendPhoto(String botToken, String chatId, String caption, String imageUrl) throws SocialPublishException {
        try {
            String url = EITAA_API_BASE + "/" + botToken + "/sendPhoto";
            
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
                log.info("Successfully sent Eitaa photo with ID: {}", messageId);
                return messageId;
            } else {
                String errorDescription = responseJson.path("description").asText();
                throw new SocialPublishException("Eitaa API error: " + errorDescription, "EITAA", 400, false);
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
            log.warn("Failed to parse Eitaa settings", e);
        }
        return null;
    }
    
    private void handleHttpError(HttpClientErrorException e) throws SocialPublishException {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        
        if (status == HttpStatus.UNAUTHORIZED) {
            throw new SocialPublishException("Bot token is invalid", "EITAA", 401, false, e);
        }
        
        if (status == HttpStatus.BAD_REQUEST) {
            throw new SocialPublishException("Bad request: " + responseBody, "EITAA", 400, false, e);
        }
        
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            throw new SocialPublishException("Rate limit exceeded", "EITAA", 429, true, e);
        }
        
        // Server errors are retryable
        if (status.is5xxServerError()) {
            throw new SocialPublishException("Server error: " + responseBody, "EITAA", status.value(), true, e);
        }
    }
    
    @Override
    public boolean validateAccount(SocialAccount account) {
        try {
            String botToken = cryptoUtil.decrypt(account.getAccessToken());
            String url = EITAA_API_BASE + "/" + botToken + "/getMe";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            return responseJson.path("ok").asBoolean();
        } catch (Exception e) {
            log.warn("Failed to validate Eitaa account: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public SocialAccount.SocialPlatform getSupportedPlatform() {
        return SocialAccount.SocialPlatform.EITAA;
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
            content.append("📰 ").append(request.getTitle());
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
        
        // Eitaa has similar limits to Telegram
        if (content.length() > 4096) {
            String text = content.toString();
            content = new StringBuilder(text.substring(0, 4090) + "...");
        }
        
        return content.toString();
    }
}
