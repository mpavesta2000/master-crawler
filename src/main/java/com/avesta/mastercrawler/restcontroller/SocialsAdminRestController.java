package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.service.ISocialAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/socials/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('Admin')")
public class SocialsAdminRestController {

    private final ISocialAccountService socialAccountService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/bot")
    public ResponseEntity<Map<String, String>> configureBotAccount(@RequestBody BotConfigRequest request) {
        try {
            // Validate platform
            SocialAccount.SocialPlatform platform = SocialAccount.SocialPlatform.valueOf(request.getPlatform().toUpperCase());
            
            if (platform == SocialAccount.SocialPlatform.X) {
                return ResponseEntity.badRequest().body(Map.of("error", "X platform requires OAuth, not bot token"));
            }

            // Create settings JSON
            Map<String, Object> settings = new HashMap<>();
            if (request.getChatId() != null) {
                settings.put("chatId", request.getChatId());
            }
            if (request.getChannelName() != null) {
                settings.put("channelName", request.getChannelName());
            }
            String settingsJson = objectMapper.writeValueAsString(settings);

            // Create or update global account
            SocialAccount account = socialAccountService.createGlobalAccount(platform, request.getBotToken(), settingsJson);
            account.setAccountName(request.getAccountName());
            socialAccountService.save(account);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Bot account configured successfully");
            response.put("accountId", account.getId().toString());
            
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid platform: " + request.getPlatform()));
        } catch (Exception e) {
            log.error("Error configuring bot account", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/global-accounts")
    public ResponseEntity<List<SocialAccount>> getGlobalAccounts() {
        try {
            List<SocialAccount> accounts = socialAccountService.findGlobalAccounts();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error getting global accounts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/test-bot")
    public ResponseEntity<Map<String, Object>> testBotAccount(@RequestBody BotTestRequest request) {
        try {
            SocialAccount.SocialPlatform platform = SocialAccount.SocialPlatform.valueOf(request.getPlatform().toUpperCase());
            
            // Create temporary account for testing
            Map<String, Object> settings = new HashMap<>();
            settings.put("chatId", request.getChatId());
            String settingsJson = objectMapper.writeValueAsString(settings);

            SocialAccount tempAccount = SocialAccount.builder()
                    .platform(platform)
                    .accessToken(request.getBotToken()) // Will be encrypted
                    .settings(settingsJson)
                    .build();

            // Validate account (this will test the bot token)
            boolean isValid = socialAccountService.validateAccount(tempAccount.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Bot token is valid" : "Bot token is invalid or chat ID not accessible");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error testing bot account", e);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Error testing bot: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Map<String, String>> deleteGlobalAccount(@PathVariable Long accountId) {
        try {
            socialAccountService.delete(accountId);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting account {}", accountId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error deleting account"));
        }
    }

    // Request DTOs
    public static class BotConfigRequest {
        private String platform;
        private String botToken;
        private String chatId;
        private String channelName;
        private String accountName;

        // Getters and setters
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getBotToken() { return botToken; }
        public void setBotToken(String botToken) { this.botToken = botToken; }
        public String getChatId() { return chatId; }
        public void setChatId(String chatId) { this.chatId = chatId; }
        public String getChannelName() { return channelName; }
        public void setChannelName(String channelName) { this.channelName = channelName; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
    }

    public static class BotTestRequest {
        private String platform;
        private String botToken;
        private String chatId;

        // Getters and setters
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getBotToken() { return botToken; }
        public void setBotToken(String botToken) { this.botToken = botToken; }
        public String getChatId() { return chatId; }
        public void setChatId(String chatId) { this.chatId = chatId; }
    }
}
