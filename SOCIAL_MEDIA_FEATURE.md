# Social Media Publishing Feature

## Overview

A comprehensive social media publishing system that allows editors to automatically post news articles to multiple social platforms (X/Twitter, Telegram, Eitaa, and Bale) directly from the news creation interface.

## Features

### ✅ Platform Support
- **X (Twitter)**: Full OAuth2 with PKCE implementation
- **Telegram**: Bot API integration with channel/group support
- **Eitaa**: Bot API integration similar to Telegram
- **Bale**: Bot API integration for Iranian messaging platform

### ✅ Publishing Options
- **Quick Post**: One-click posting to X or Bale
- **Batch Publishing**: Multi-platform modal with advanced options
- **Scheduled Publishing**: Queue jobs for future publication
- **Custom Messages**: Add custom text before news content

### ✅ Content Management
- **Smart Formatting**: Platform-specific content formatting
- **Image Support**: Automatic image attachment where supported
- **URL Inclusion**: Optional news URL appending
- **Character Limits**: Automatic truncation with smart ellipsis

### ✅ Job Management
- **Async Processing**: Non-blocking job queue with retry logic
- **Real-time Status**: Live polling of publication status
- **Error Handling**: Comprehensive error reporting and retry mechanisms
- **Admin Monitoring**: Full visibility into job statuses

### ✅ Security & Configuration
- **Encrypted Storage**: All tokens stored with AES encryption
- **OAuth Flow**: Secure X authentication with PKCE
- **Admin Controls**: Site-wide bot configuration for messaging platforms
- **User Accounts**: Per-user social account management

## File Structure

```
src/main/java/com/avesta/mastercrawler/
├── model/
│   ├── SocialAccount.java              # Social account entity
│   └── SocialPublishJob.java           # Publish job entity
├── repository/
│   ├── SocialAccountRepository.java    # Social account repository
│   └── SocialPublishJobRepository.java # Job repository
├── service/
│   ├── social/
│   │   ├── SocialAdapter.java          # Adapter interface
│   │   ├── SocialPublisherService.java # Main publishing service
│   │   ├── XAdapter.java               # X/Twitter adapter
│   │   ├── TelegramAdapter.java        # Telegram adapter
│   │   ├── EitaaAdapter.java           # Eitaa adapter
│   │   ├── BaleAdapter.java            # Bale adapter
│   │   └── SocialPublishException.java # Custom exception
│   ├── ISocialAccountService.java      # Service interface
│   └── impl/
│       └── SocialAccountServiceImpl.java # Service implementation
├── controller/
│   ├── SocialsController.java          # UI controller
│   └── XOAuthController.java           # OAuth controller
├── restcontroller/
│   ├── SocialsRestController.java      # REST API
│   └── SocialsAdminRestController.java # Admin API
├── dto/
│   ├── SocialPublishRequest.java       # Request DTO
│   └── SocialPublishResponse.java      # Response DTO
├── utility/
│   └── CryptoUtil.java                 # Encryption utility
└── configuration/
    └── SocialConfiguration.java        # Configuration class

src/main/resources/
├── templates/socials/
│   ├── socials-home.html               # Main socials page
│   ├── socials-x.html                  # X configuration page
│   ├── socials-telegram.html           # Telegram setup page
│   ├── socials-eitaa.html              # Eitaa setup page
│   ├── socials-bale.html               # Bale setup page
│   └── social-modal.html               # Publishing modal
└── static/assets/js/socials/
    └── social-publisher.js              # Frontend JavaScript

src/test/java/com/avesta/mastercrawler/
├── service/social/
│   ├── SocialPublisherServiceTest.java # Service tests
│   └── XAdapterTest.java               # Adapter tests
└── restcontroller/
    └── SocialsRestControllerTest.java   # Controller tests
```

## Setup Instructions

### 1. Database Configuration

The system will automatically create the required tables:
- `social_accounts`: Stores encrypted social media account credentials
- `social_publish_jobs`: Tracks publication job status and history

### 2. Application Properties

Update `application.properties`:

```properties
# Social Media Publishing Configuration
app.encryption.key=YourSecureEncryptionKey123!@#$%

# X (Twitter) OAuth Configuration  
x.oauth.client-id=your_x_client_id_here
x.oauth.client-secret=your_x_client_secret_here
x.oauth.redirect-uri=http://localhost:8081/socials/x/callback
```

### 3. X (Twitter) Setup

1. Create a Twitter Developer App at [developer.x.com](https://developer.x.com)
2. Enable OAuth 2.0 with PKCE
3. Add callback URL: `http://localhost:8081/socials/x/callback`
4. Required permissions: `tweet.read`, `tweet.write`, `users.read`
5. Update application.properties with your client ID and secret

### 4. Bot Platform Setup

#### Telegram:
1. Create bot via @BotFather
2. Get bot token
3. Add bot to channel/group with admin rights
4. Get chat ID using @userinfobot or @RawDataBot

#### Eitaa:
1. Request bot from Eitaa support
2. Get bot token and API access
3. Configure channel/group permissions

#### Bale:
1. Create bot via @BotFather (Bale)
2. Get bot token
3. Add bot to channel/group
4. Get chat ID

## Usage Guide

### For End Users

1. **Quick Publishing**:
   - Click "ارسال به ایکس" (Post to X) or "ارسال به بله" (Post to Bale) 
   - News is immediately queued for publication

2. **Advanced Publishing**:
   - Click "انتشار گروهی" (Batch Publish) 
   - Select platforms, customize message, schedule time
   - Click "انتشار" (Publish)

3. **Status Monitoring**:
   - Watch real-time status updates
   - Green checkmarks indicate successful publication
   - Red errors show detailed failure reasons

### For Administrators

1. **Platform Setup**:
   - Visit `/socials` in admin panel
   - Configure bot tokens for Telegram/Eitaa/Bale
   - Test configurations before saving

2. **User Management**:
   - Monitor all social accounts
   - Deactivate/activate accounts as needed
   - View publication history and job statuses

## API Endpoints

### Public Endpoints
- `POST /api/socials/publish` - Publish content to social platforms
- `GET /api/socials/status/{jobId}` - Get job status
- `GET /api/socials/news/{newsId}/statuses` - Get all jobs for news
- `GET /api/socials/accounts` - Get user's social accounts

### Admin Endpoints
- `POST /api/socials/admin/bot` - Configure global bot accounts
- `GET /api/socials/admin/global-accounts` - List global accounts
- `POST /api/socials/admin/test-bot` - Test bot configuration
- `DELETE /api/socials/admin/accounts/{id}` - Delete account

### OAuth Endpoints
- `GET /socials/x/connect` - Initiate X OAuth flow
- `GET /socials/x/callback` - Handle OAuth callback

## Security Considerations

1. **Token Encryption**: All access tokens encrypted with AES
2. **PKCE Implementation**: X OAuth uses PKCE for enhanced security
3. **Admin Authorization**: Bot configuration requires admin role
4. **Rate Limiting**: Built-in retry logic respects platform limits
5. **Error Isolation**: Failed jobs don't affect other publications

## Error Handling

The system includes comprehensive error handling:
- **Retryable Errors**: Network issues, rate limits (auto-retry)
- **Non-retryable Errors**: Invalid tokens, permissions (immediate failure)
- **User Feedback**: Clear error messages in Persian
- **Admin Monitoring**: All errors logged with full context

## Job Processing

- **Async Processing**: Jobs processed every 30 seconds
- **Retry Logic**: Up to 3 retries for failed jobs
- **Status Tracking**: Real-time status updates via polling
- **Cleanup**: Auto-cleanup of old job records (configurable)

## Performance Optimization

- **Parallel Processing**: Multiple platform adapters run concurrently
- **Smart Queueing**: Immediate processing for urgent posts
- **Caching**: Account validation results cached
- **Polling Optimization**: Smart polling intervals based on job status

## Testing

Run the comprehensive test suite:

```bash
mvn test -Dtest="*Social*Test"
```

Tests cover:
- Service layer functionality
- Adapter implementations  
- REST API endpoints
- Error scenarios
- OAuth flows

## Monitoring & Maintenance

1. **Job Queue**: Monitor `social_publish_jobs` table for stuck jobs
2. **Token Expiry**: X tokens may expire, users need to reconnect
3. **Bot Status**: Test bot connections periodically
4. **Error Logs**: Monitor application logs for publishing failures

## Future Enhancements

- **Analytics Dashboard**: Publication success rates, engagement metrics
- **Content Templates**: Predefined templates for different news types
- **A/B Testing**: Test different content formats
- **Webhook Support**: Real-time notifications for job completion
- **Image Optimization**: Automatic image resizing for platforms

## Troubleshooting

### Common Issues

1. **"No configured account found"**
   - Solution: Ensure user has connected their social accounts

2. **"Bot token is invalid"**  
   - Solution: Verify bot token and permissions in platform settings

3. **"Authentication failed - token may be expired"**
   - Solution: Reconnect X account via OAuth flow

4. **Jobs stuck in "Processing"**
   - Solution: Check network connectivity and platform API status

5. **Character limit exceeded**
   - Solution: Content automatically truncated, but verify formatting

For additional support, check the application logs and contact the development team.
