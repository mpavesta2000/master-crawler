package com.avesta.mastercrawler.service.social;

public class SocialPublishException extends Exception {
    
    private final String platform;
    private final int errorCode;
    private final boolean retryable;
    
    public SocialPublishException(String message, String platform) {
        super(message);
        this.platform = platform;
        this.errorCode = 0;
        this.retryable = true;
    }
    
    public SocialPublishException(String message, String platform, Throwable cause) {
        super(message, cause);
        this.platform = platform;
        this.errorCode = 0;
        this.retryable = true;
    }
    
    public SocialPublishException(String message, String platform, int errorCode, boolean retryable) {
        super(message);
        this.platform = platform;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    public SocialPublishException(String message, String platform, int errorCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.platform = platform;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
}
