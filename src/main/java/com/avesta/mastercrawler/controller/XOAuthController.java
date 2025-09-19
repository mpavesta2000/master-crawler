package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.ISocialAccountService;
import com.avesta.mastercrawler.utility.CustomUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/socials/x")
@RequiredArgsConstructor
@Slf4j
public class XOAuthController {

    private final ISocialAccountService socialAccountService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${x.oauth.client-id:your_client_id}")
    private String clientId;

    @Value("${x.oauth.client-secret:your_client_secret}")
    private String clientSecret;

    @Value("${x.oauth.redirect-uri:http://localhost:8081/socials/x/callback}")
    private String redirectUri;

    private static final String X_AUTH_URL = "https://x.com/i/oauth2/authorize";
    private static final String X_TOKEN_URL = "https://api.x.com/2/oauth2/token";
    private static final String X_USER_URL = "https://api.x.com/2/users/me";

    @GetMapping("/connect")
    public String initiateOAuth(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            // Generate PKCE parameters
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state = generateState();

            // Store in session
            session.setAttribute("code_verifier", codeVerifier);
            session.setAttribute("oauth_state", state);

            // Build authorization URL
            StringBuilder authUrl = new StringBuilder(X_AUTH_URL);
            authUrl.append("?response_type=code");
            authUrl.append("&client_id=").append(clientId);
            authUrl.append("&redirect_uri=").append(redirectUri);
            authUrl.append("&scope=tweet.read%20tweet.write%20users.read");
            authUrl.append("&state=").append(state);
            authUrl.append("&code_challenge=").append(codeChallenge);
            authUrl.append("&code_challenge_method=S256");

            log.info("Redirecting to X OAuth: {}", authUrl.toString());
            return "redirect:" + authUrl.toString();

        } catch (Exception e) {
            log.error("Error initiating OAuth", e);
            redirectAttributes.addFlashAttribute("error", "Failed to initiate X connection: " + e.getMessage());
            return "redirect:/socials/x";
        }
    }

    @GetMapping("/callback")
    public String handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Check for OAuth errors
            if (error != null) {
                log.error("OAuth error: {}", error);
                redirectAttributes.addFlashAttribute("error", "X authorization failed: " + error);
                return "redirect:/socials/x";
            }

            // Validate state parameter
            String sessionState = (String) session.getAttribute("oauth_state");
            if (sessionState == null || !sessionState.equals(state)) {
                log.error("Invalid state parameter");
                redirectAttributes.addFlashAttribute("error", "Security validation failed");
                return "redirect:/socials/x";
            }

            // Get code verifier from session
            String codeVerifier = (String) session.getAttribute("code_verifier");
            if (codeVerifier == null) {
                log.error("Code verifier not found in session");
                redirectAttributes.addFlashAttribute("error", "Session expired, please try again");
                return "redirect:/socials/x";
            }

            // Exchange code for tokens
            TokenResponse tokenResponse = exchangeCodeForTokens(code, codeVerifier);
            if (tokenResponse == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to obtain access token");
                return "redirect:/socials/x";
            }

            // Get user info
            UserInfo userInfo = getUserInfo(tokenResponse.getAccessToken());
            if (userInfo == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to get user information");
                return "redirect:/socials/x";
            }

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Users currentUser = userDetails.getUser();

            // Create or update social account
            SocialAccount account = socialAccountService.createAccount(
                    currentUser,
                    SocialAccount.SocialPlatform.X,
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    null
            );

            account.setAccountName("@" + userInfo.getUsername());
            account.setAccountId(userInfo.getId());
            socialAccountService.save(account);

            // Clean up session
            session.removeAttribute("code_verifier");
            session.removeAttribute("oauth_state");

            redirectAttributes.addFlashAttribute("success", "Successfully connected X account: @" + userInfo.getUsername());
            return "redirect:/socials/x";

        } catch (Exception e) {
            log.error("Error handling OAuth callback", e);
            redirectAttributes.addFlashAttribute("error", "Failed to connect X account: " + e.getMessage());
            return "redirect:/socials/x";
        }
    }

    private TokenResponse exchangeCodeForTokens(String code, String codeVerifier) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Create Basic Auth header
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", clientId);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            body.add("code_verifier", codeVerifier);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    X_TOKEN_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Token exchange response status: {}", response.getStatusCode());
            log.info("Token exchange response body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String accessToken = jsonResponse.path("access_token").asText();
                String tokenType = jsonResponse.path("token_type").asText();
                String scope = jsonResponse.path("scope").asText();
                
                log.info("Received token type: {}", tokenType);
                log.info("Received token scope: {}", scope);
                log.info("Access token length: {}", accessToken != null ? accessToken.length() : "null");
                log.info("Access token starts with: {}", accessToken != null && accessToken.length() > 10 ? accessToken.substring(0, 10) + "..." : "null");
                
                return new TokenResponse(
                        accessToken,
                        jsonResponse.path("refresh_token").asText(null),
                        jsonResponse.path("expires_in").asInt(0)
                );
            }

            log.error("Token exchange failed: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("Error exchanging code for tokens", e);
            return null;
        }
    }

    private UserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    X_USER_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode userData = jsonResponse.path("data");
                
                return new UserInfo(
                        userData.path("id").asText(),
                        userData.path("username").asText(),
                        userData.path("name").asText()
                );
            }

            log.error("User info request failed: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("Error getting user info", e);
            return null;
        }
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private String generateState() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] state = new byte[16];
        secureRandom.nextBytes(state);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(state);
    }

    // Inner classes for responses
    private static class TokenResponse {
        private final String accessToken;
        private final String refreshToken;
        private final int expiresIn;

        public TokenResponse(String accessToken, String refreshToken, int expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public int getExpiresIn() { return expiresIn; }
    }

    private static class UserInfo {
        private final String id;
        private final String username;
        private final String name;

        public UserInfo(String id, String username, String name) {
            this.id = id;
            this.username = username;
            this.name = name;
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getName() { return name; }
    }
}
