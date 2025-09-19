package com.avesta.mastercrawler.service.social;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.dto.SocialPublishRequest;

public interface SocialAdapter {
    
    /**
     * Post content to the social platform
     * @param request The content to post
     * @param account The social account to use for posting
     * @return The platform-specific post ID or response
     * @throws SocialPublishException if posting fails
     */
    String post(SocialPublishRequest request, SocialAccount account) throws SocialPublishException;
    
    /**
     * Validate that the account credentials are still valid
     * @param account The social account to validate
     * @return true if valid, false otherwise
     */
    boolean validateAccount(SocialAccount account);
    
    /**
     * Get the platform this adapter supports
     * @return The social platform
     */
    SocialAccount.SocialPlatform getSupportedPlatform();
    
    /**
     * Format the content for the specific platform's requirements
     * @param request The original content
     * @return Formatted content suitable for the platform
     */
    String formatContent(SocialPublishRequest request);
}
