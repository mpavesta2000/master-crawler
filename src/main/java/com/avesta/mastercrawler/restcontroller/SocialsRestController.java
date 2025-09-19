package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.dto.SocialPublishRequest;
import com.avesta.mastercrawler.dto.SocialPublishResponse;
import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.ISocialAccountService;
import com.avesta.mastercrawler.utility.CustomUserDetails;
import com.avesta.mastercrawler.utility.CryptoUtil;
import com.avesta.mastercrawler.service.social.SocialPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/socials")
@RequiredArgsConstructor
@Slf4j
public class SocialsRestController {

    private final SocialPublisherService socialPublisherService;
    private final ISocialAccountService socialAccountService;
    private final CryptoUtil cryptoUtil;

    @PostMapping("/publish")
    public ResponseEntity<SocialPublishResponse> publishToSocials(@Valid @RequestBody SocialPublishRequest request) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Users currentUser = userDetails.getUser();

            // Validate request
            if (request.getPlatforms() == null || request.getPlatforms().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    SocialPublishResponse.builder()
                        .success(false)
                        .message("At least one platform must be specified")
                        .build()
                );
            }

            if (request.getNewsId() == null) {
                return ResponseEntity.badRequest().body(
                    SocialPublishResponse.builder()
                        .success(false)
                        .message("News ID is required")
                        .build()
                );
            }

            // Publish to socials
            SocialPublishResponse response = socialPublisherService.publishToSocials(request, currentUser);
            
            return ResponseEntity.status(response.isSuccess() ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST)
                    .body(response);

        } catch (Exception e) {
            log.error("Error publishing to socials", e);
            return ResponseEntity.internalServerError().body(
                SocialPublishResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<SocialPublishResponse.JobStatus> getJobStatus(@PathVariable Long jobId) {
        try {
            SocialPublishResponse.JobStatus status = socialPublisherService.getJobStatus(jobId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting job status for job {}", jobId, e);
            return ResponseEntity.internalServerError().body(
                SocialPublishResponse.JobStatus.builder()
                    .jobId(jobId)
                    .status(com.avesta.mastercrawler.model.SocialPublishJob.JobStatus.FAILED)
                    .errorMessage("Error retrieving status: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/news/{newsId}/statuses")
    public ResponseEntity<List<SocialPublishResponse.JobStatus>> getJobStatusesByNews(@PathVariable Integer newsId) {
        try {
            List<SocialPublishResponse.JobStatus> statuses = socialPublisherService.getJobStatusesByNews(newsId);
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            log.error("Error getting job statuses for news {}", newsId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<SocialAccount>> getUserAccounts() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Users currentUser = (Users) auth.getPrincipal();

            List<SocialAccount> accounts = socialAccountService.findByUser(currentUser);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error getting user accounts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/accounts/{platform}")
    public ResponseEntity<List<SocialAccount>> getUserAccountsByPlatform(@PathVariable SocialAccount.SocialPlatform platform) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Users currentUser = (Users) auth.getPrincipal();

            List<SocialAccount> accounts = socialAccountService.findByUserAndPlatform(currentUser, platform);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error getting user accounts for platform {}", platform, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/accounts/{accountId}/validate")
    public ResponseEntity<Map<String, Boolean>> validateAccount(@PathVariable Long accountId) {
        try {
            boolean isValid = socialAccountService.validateAccount(accountId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("valid", isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating account {}", accountId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/accounts/{accountId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateAccount(@PathVariable Long accountId) {
        try {
            socialAccountService.deactivate(accountId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deactivating account {}", accountId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/accounts/{accountId}/activate")
    public ResponseEntity<Map<String, String>> activateAccount(@PathVariable Long accountId) {
        try {
            socialAccountService.activate(accountId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account activated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error activating account {}", accountId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug/accounts")
    public ResponseEntity<?> debugAccounts() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Users currentUser = userDetails.getUser();

            List<SocialAccount> accounts = socialAccountService.findByUser(currentUser);
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("userEmail", currentUser.getEmail());
            debug.put("accountCount", accounts.size());
            
            List<Map<String, Object>> accountInfo = accounts.stream().map(account -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", account.getId());
                info.put("platform", account.getPlatform());
                info.put("accountName", account.getAccountName());
                info.put("tokenLength", account.getAccessToken() != null ? account.getAccessToken().length() : 0);
                info.put("isActive", account.getIsActive());
                info.put("createdAt", account.getCreatedAt());
                return info;
            }).toList();
            
            debug.put("accounts", accountInfo);
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            log.error("Error in debug endpoint", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/debug/clear-x-accounts")
    public ResponseEntity<?> clearXAccounts() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Users currentUser = userDetails.getUser();

            socialAccountService.deleteByPlatformAndUser(SocialAccount.SocialPlatform.X, currentUser);
            
            return ResponseEntity.ok(Map.of("message", "X accounts cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing X accounts", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/x-token-analysis")
    public ResponseEntity<Map<String, Object>> analyzeXToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Users currentUser = userDetails.getUser();
            
            Optional<SocialAccount> xAccount = socialAccountService.findByPlatformAndUser(SocialAccount.SocialPlatform.X, currentUser);
            
            Map<String, Object> analysis = new HashMap<>();
            
            if (xAccount.isPresent()) {
                SocialAccount account = xAccount.get();
                String encryptedToken = account.getAccessToken();
                
                analysis.put("hasAccount", true);
                analysis.put("accountName", account.getAccountName());
                analysis.put("encryptedTokenLength", encryptedToken != null ? encryptedToken.length() : 0);
                
                try {
                    // Decrypt token for analysis
                    String decryptedToken = cryptoUtil.decrypt(encryptedToken);
                    analysis.put("decryptedTokenLength", decryptedToken != null ? decryptedToken.length() : 0);
                    analysis.put("tokenStartsWith", decryptedToken != null && decryptedToken.length() > 10 ? 
                            decryptedToken.substring(0, 10) + "..." : "null");
                    
                    // Analyze token characteristics
                    if (decryptedToken != null) {
                        analysis.put("tokenContainsDash", decryptedToken.contains("-"));
                        analysis.put("tokenContainsUnderscore", decryptedToken.contains("_"));
                        analysis.put("tokenIsAlphanumeric", decryptedToken.matches("[a-zA-Z0-9]+"));
                        analysis.put("tokenHasBase64Chars", decryptedToken.matches(".*[+/=].*"));
                        
                        // Check if it looks like a Bearer token (typically starts with specific patterns)
                        analysis.put("looksLikeBearerToken", 
                                decryptedToken.startsWith("AAAAAAAAAAAAAAAA") || 
                                decryptedToken.length() < 200);
                        
                        // Check if it looks like OAuth 2.0 User Context token (typically much longer)
                        analysis.put("looksLikeUserContextToken", 
                                decryptedToken.length() > 300 && 
                                !decryptedToken.startsWith("AAAAAAAAAAAAAAAA"));
                    }
                    
                } catch (Exception e) {
                    analysis.put("decryptionError", e.getMessage());
                }
                
                analysis.put("createdAt", account.getCreatedAt());
                analysis.put("expiresAt", account.getExpiresAt());
                
            } else {
                analysis.put("hasAccount", false);
                analysis.put("message", "No X account found for current user");
            }
            
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing X token", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
