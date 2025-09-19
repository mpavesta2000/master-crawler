package com.avesta.mastercrawler.dto;

import com.avesta.mastercrawler.model.SocialAccount;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SocialPublishRequest {
    private Integer newsId;
    private String title;
    private String lead;
    private String content;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String imageUrl;
    private List<String> tags;
    private String newsUrl;
    private List<SocialAccount.SocialPlatform> platforms;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    private PublishSettings settings;
    
    @Data
    public static class PublishSettings {
        private boolean includeImage = true;
        private boolean includeUrl = true;
        private String customMessage;
        private int maxLength = 280; // Default for X
    }
}
