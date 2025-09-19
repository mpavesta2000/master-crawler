
## 📋 **The Problem**
According to X API documentation, **Bearer Tokens (Application-Only)** cannot post tweets. You need **OAuth 2.0 User Context tokens** from the Authorization Code Flow with PKCE.

## 🚨 **Current Issue**
Your database still contains the Bearer token you provided earlier, not a proper User Context token from OAuth flow.

## ✅ **Step-by-Step Solution**

### **Step 1: Clear Current Bearer Token**
1. **Go to:** [http://localhost:8081/api/socials/debug/accounts](http://localhost:8081/api/socials/debug/accounts)
   - This will show you what's currently in the database
   - Check `tokenLength` - if it's around 120-140 characters, it's likely the Bearer token

2. **Clear X accounts:** Send DELETE request to [http://localhost:8081/api/socials/debug/clear-x-accounts](http://localhost:8081/api/socials/debug/clear-x-accounts)
   - Or manually delete from `/socials/x` page if there's a disconnect button

### **Step 2: Verify Twitter App Settings**
1. **Go to:** [https://developer.x.com/en/portal/dashboard](https://developer.x.com/en/portal/dashboard)
2. **Click your app** (the one with Client ID: `QmwyQWhLdmZvZHRwOWZ5MHBxX0s6MTpjaQ`)
3. **App Settings → User authentication settings:**
   - ✅ **OAuth 2.0:** Should be enabled
   - ✅ **Permissions:** Should be "Read and Write"
   - ✅ **Callback URL:** Should be `http://localhost:8081/socials/x/callback`
   - ✅ **Website URL:** Add `http://localhost:8081` if required

### **Step 3: Get User Context Token via OAuth Flow**
1. **Go to:** [http://localhost:8081/socials/x](http://localhost:8081/socials/x)
2. **Click "Connect to X"** button
3. **You'll be redirected to X** - authorize your app
4. **After authorization**, you'll be redirected back with success message
5. **Verify:** Check [http://localhost:8081/api/socials/debug/accounts](http://localhost:8081/api/socials/debug/accounts) again
   - The new token should be different from the Bearer token

### **Step 4: Test Tweet Posting**
1. **Go to:** News add page and try posting to X
2. **Should work** with the new User Context token

## 🔍 **Verification Commands**

```bash
# Check what's in database
curl http://localhost:8081/api/socials/debug/accounts

# Clear X accounts (if needed)
curl -X DELETE http://localhost:8081/api/socials/debug/clear-x-accounts
```

## 📚 **Key Documentation References**
- [X OAuth 2.0 Overview](https://docs.x.com/fundamentals/authentication/oauth-2-0/overview)
- [Bearer Token Limitations](https://docs.x.com/fundamentals/authentication/oauth-2-0/application-only)

## ⚠️ **Important Notes**
- **Bearer Tokens are for READ-ONLY** access to public data
- **User Context Tokens are required for POSTING** tweets
- The OAuth flow must complete successfully to get a User Context token
- Make sure your Twitter app has "Read and Write" permissions

---
