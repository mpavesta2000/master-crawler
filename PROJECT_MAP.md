# Master Crawler — Project Map

Snapshot of `main` @ `5ebf481` (2026-11-18). This covers what the project is, where each part lives, what it does, and what is broken or risky.

---

## 1. What this project is

A **Persian-language newsroom CMS** built with Spring Boot 3.5.4 / Java 21 / Thymeleaf / MySQL. It has three main jobs:

1. **Get news in.** It scrapes Arabic news sites (Selenium + Jsoup), polls RSS feeds, and calls the external ChapChin API. AI rewrites each story in Persian and saves it as a **Draft**.
2. **Help editors write.** Editors use AI tools: rewrite, headline and lead variants, tags, meta, slug, alt text, translation, voice-to-text, and question-based generation. They also get Yoast SEO scoring, a gallery, categories, tags, comments, homepage ordering, and analytics.
3. **Get news out.** Posts go to Telegram, Bale, Eitaa and X through a job queue, and the public **"Zolal"** site serves readers at `/`.

Despite the name, crawling is only one part. Most of the code is CMS.

---

## 2. Top-level layout

| Path | What it is | Notes |
|---|---|---|
| `pom.xml`, `mvnw*`, `.mvn/` | Maven build (Spring Boot 3.5.4, Java 21) | Builds cleanly offline |
| `src/main/java/com/avesta/mastercrawler/` | All backend code (~150 classes, ~13k lines) | See §3 |
| `src/main/resources/templates/` | Thymeleaf views (admin CMS + `zolal/` public site) | See §5 |
| `src/main/resources/static/assets/` | CSS/JS/fonts/images/vendor libs (**~155 MB**) | Mostly the purchased admin theme (Velzon-style); see §5 |
| `src/main/resources/application.properties` | **Missing.** It is gitignored and not on disk | The app cannot start without it; see §7 |
| `src/test/` | 4 test classes | See §6 |
| `news/photos/`, `news/videos/` | Runtime upload folder (served at `/news/**`) | **Tracked in git** (25 MB, including two `.mkv` files) |
| `node_modules/`, `package.json` | Only used to bundle YoastSEO once (esbuild) | **2,325 files tracked in git**, including a macOS-only esbuild binary |
| `README.md` | Generic readme | Outdated (says PostgreSQL, wrong folder names, links a LICENSE that doesn't exist) |
| `SOCIAL_MEDIA_FEATURE.md` | Design doc for the social publishing feature | Accurate overall |
| `.DS_Store` ×61 | macOS junk | Tracked in git |

---

## 3. Backend — package by package

### Entry & configuration (`configuration/`, `audit/`)
| Class | Does |
|---|---|
| `MasterCrawlerApplication` | Main class; enables JPA auditing |
| `SecurityConfig` | Form login at `/admin/login`, public URL list, blocks **Ai** users from user/comment/sort pages, CSRF **disabled** |
| `CustomAuthenticationSuccessHandler` | Admin → `/admin/full-users/report`; User/Ai → `/admin/user/report` |
| `CustomAuthenticationFailureHandler` | Persian error message → back to login |
| `DataBaseSeeder` | On first run, seeds NewsType (Image/Video/Default), UsersType (Admin/User/Ai), ~50 Persian categories, and an empty SiteSetting |
| `LangChainConfig` | One `ChatLanguageModel` (LangChain4j OpenAI client pointed at a Gemini-compatible URL); `maxOutputTokens` default 4096 |
| `SeleniumConfig` | Prototype `WebDriver` bean. **Unused**: every scraper builds its own ChromeDriver |
| `WebSocketConfig` | STOMP `/ws` + `/topic`, used to stream RSS logs to the browser |
| `AsyncConfig`, `SocialConfiguration` | Thread pool, `@EnableAsync`, `@EnableScheduling` |
| `MvcConfig` | Serves `./news/**` from disk |
| `RestTemplateConfig` | `RestTemplate` bean |
| `AuditAwareImpl` | Auditor is always `"SYSTEM_OPERATOR"`, so `createdBy`/`updatedBy` never record the real user |

### Data model (`model/`, 18 entities, MySQL)
- **News**: the main entity. Title, lead, subtitle, headline, body (HTML), meta/SEO fields, slug, Yoast score, main image/video, image list, flags (breaking / headline / slider / most-viewed / comments / translated / ChapChin), status, type, categories, tags, comments, owner (`userId`).
- **Users**, **UsersType** (Admin / User / Ai), **UserProfile**, **UserMonthlyReport** (per-month counters for analytics).
- **Category** (self-parent tree), **Tags** (owned by a user), **Comments**, **NewsType**.
- **Images**, **Videos**: gallery records that point to files in `news/`.
- **SiteSetting**: ordered ID lists for the breaking / slider / headline / most-viewed homepage sections.
- **SocialAccount** (encrypted tokens, per-user or global bot), **SocialPublishJob** (queue with retries).
- **AboutUs**, **CallUs**, **Links**: entity, repo and service exist, but **nothing uses them** (dead code; `links-*.js` is dead too).

`repository/` has one Spring Data repo per entity. `service/I*Service` + `service/impl/*` is a thin CRUD layer over those repos.

### Admin pages (`controller/`)
| URL prefix | Controller | Purpose |
|---|---|---|
| `/admin/login`, `/admin/logout`, `/admin/signup/4f8e…`, `/admin/change/password` | `AuthenticationController` | Login, hidden signup, password reset |
| `/admin/full-users/report`, `/admin/user/report` | `AnalyticsController` | Admin-wide and per-user dashboards |
| `/admin/news/*` | `NewsController` (833 lines) | Add/edit text, image and video news; list; search; delete; homepage sort; preview |
| `/admin/category/*`, `/admin/tags/*`, `/admin/comments/*` | Category/Tags/Comments controllers | CRUD |
| `/admin/gallery/*` | `GalleryController` | Image/video library |
| `/admin/quickNews/{crawler,generate,text,translate}` | `CrawlerController` | ChapChin crawler UI, AI generator, voice-to-text, translate |
| `/admin/rss/setting` | `RssController` | Start/stop RSS pollers, live log |
| `/admin/profile/*`, `/admin/users/admins/list` | Profile/Users controllers | Profile, password, user list |
| `/socials`, `/socials/{x,telegram,eitaa,bale}`, `/socials/x/{connect,callback}` | `SocialsController`, `XOAuthController` | Connect accounts (X uses OAuth2 PKCE) |
| `/`, `/news`, `/gallery`, `/videos`, `/archive`, `/search`, `/about`, … | `ZolalSiteController` | Public site. **Static design sample only; nothing reads the DB yet** |
| (all views) | `GlobalControllerAdvice` | Adds the logged-in `userProfile` to every model |

### JSON APIs (`restcontroller/` + `service/ai/NewsGeneratorRestController`)
| Endpoint | Purpose |
|---|---|
| `POST /api/chapchin/send` | Scrape a URL → AI-rewrite → save as Draft |
| `POST /api/gemini/{modify,generate-slug,generate-alt-text}` | AI editing helpers |
| `GET/POST /api/news-generator/{questions,generate}` | Question-driven article generation |
| `POST /api/translate/{news,save}` | Translate an article |
| `POST /api/upload/{voice,audio}`, `/api/getvoice`, `DELETE /api/delete/audio` | Whisper transcription (avalai.ir, `groq.whisper-large-v3`) |
| `POST /api/{image,video}/upload`, `GET /api/{image,video}/fetch`, `/api/gallery/images/upload`, `/api/profile/picture/upload` | Uploads |
| `GET /api/data`, `/api/comments/data`, `/api/tags/{data,search}`, `/api/users/admin/data` | DataTables feeds |
| `POST /api/news/find` (**public**), `/api/news/{section}/save`, `…/order/save`, `/api/news/delete/{section}/{id}` | Homepage section ordering |
| `POST/GET /api/{borna,fararu,bbcarabic,alwatan}-rss/{toggle,max-news}` | RSS pollers |
| `/api/socials/*` | Publish, job status, account validate/activate/deactivate, **debug endpoints** |
| `/api/socials/admin/*` | Global bot management (meant to be Admin-only) |

`NewsGeneratorRestController` lives in `service/ai/` instead of `restcontroller/`, which is misplaced but works.

### Ingestion pipeline (`service/scraper/`, `service/rss/`)
```
RSS poller (Borna, Fararu, BBC Arabic, Al-Watan) ─┐
Crawler UI / POST /api/chapchin/send ─────────────┤
                                                  ▼
                              ChapChinService.chapChinResponse(url)
        ├─ alarabiya / aljazeera / bbc.com/arabic / ahram / almayadeen / al-watan → own Selenium scraper
        └─ anything else → POST https://chapchin.net/api/pull/legacy
                                                  ▼
                              ChapChinService.chapChinSaveNews(json)
        GeminiService rewrites title, body, lead, tags, meta, headline (≈7 LLM calls per story)
        ImageDownloadUtil pulls body images into news/photos; the cover image is downloaded too
        Saved as News{status=Draft, ChapChin=true, user=#1, type=#3}
```
- RSS pollers are singletons with their own `ScheduledExecutorService` (1-minute interval, 1–20 item cap). They copy the security context of whoever pressed Start and stream logs over `/topic/logs`.

### AI (`service/ai/`)
- `GeminiService`: about 40 Persian prompt presets (title, lead and headline variants, tags, meta, slug, alt text…). It asks the model to wrap the answer in `<div id="answer">` and parses that out.
- `WhisperService`: speech-to-text.

### Social publishing (`service/social/`)
- `SocialAdapter` interface with `TelegramAdapter`, `BaleAdapter`, `EitaaAdapter`, `XAdapter` (API v2 + media upload).
- `SocialPublisherService` queues `SocialPublishJob`s. A `@Scheduled` job every 30 s processes queued jobs and retries failures (max 3).
- Tokens are AES-encrypted via `CryptoUtil`.

### External CMS (`service/cms/AasaamCmsUploader`)
- Pushes news to **tinn.ir** (Aasaam). The upload calls are commented out, but `getCategories()` still makes a live HTTP call **every time** the add/edit news page loads.

### Utilities (`utility/`, `dto/`, `mapper/`)
`DataScope` (Ai users see only their own news, tags and report), `CustomUserDetails`, `CryptoUtil`, `FileUploadUtil`, `ImageDownloadUtil`, `VideoConverter`, plus request/response DTOs and `AdminMapper`.

---

## 4. Roles

| Role | Sees |
|---|---|
| **Admin** | Everything |
| **User** | Everything except what Admin-only checks block (and those checks don't currently work; see §7) |
| **Ai** | Only their own news, tags and report. Blocked from user, comment and sort pages and those APIs. |

---

## 5. Frontend

- **Admin UI**: `templates/layout.html` + `partials/*` (sidebar, topbar, head-css, vendor-scripts…). Pages are in `analytics/`, `news/`, `crawler/`, `gallery/`, `categories/`, `tags/`, `rss/`, `socials/`, `users/`, `authentication/`. Page logic is in `static/assets/js/crawler/*.js` (one file per page) and `assets/js/socials/`.
- **Yoast SEO**: pre-bundled `yoastSEO.js` and `yoastSEO-new-ui.js` (2.4 MB each, *different* builds) in `assets/js/crawler/`.
- **Public Zolal site**: `templates/zolal/*` + `static/assets/zolal/{css/site.css, js/site.js, img/*}`. Static mock content.
- **Theme bloat**: `assets/libs` (91 MB, 49 libs), `assets/images` (28 MB, including NFT demo GIFs), and `assets/js/pages` (dozens of theme demo `*.init.js`). Most of this is unused theme demo material.

**Templates that nothing renders** (theme leftovers or abandoned):
`authentication/*` except signin-cover, signup-cover and pass-reset-basic (20 files) · `crawler/crawler`, `crawler/generate-news`, `crawler/prompt-setting` · `layouts/*` (4) · `users/profile`.

**Views a controller returns that don't exist:**
`ckeditor-filemanager/filemanager` (`GET /admin/gallery/gallery-browser` will 500). `/access-denied` is configured as the access-denied page but has no mapping, so denied users get an error page. There is no `templates/error/` folder, so all errors show the Whitelabel page.

---

## 6. Build & tests (verified 2026-11-18)

- `./mvnw -o test-compile` → **compiles cleanly** (161 classes).
- Unit tests (`SocialPublisherServiceTest`, `XAdapterTest`, `SocialsRestControllerTest`): **14 run, 9 pass, 1 fail, 4 error**.
  - `XAdapterTest.testFormatContent_ExceedsLimit` fails because the "long" title is only 220 chars. It fits in 280 with the URL, so no `...` is added. The test data is wrong, not the adapter.
  - `SocialsRestControllerTest` (all 4) errors because the `@WebMvcTest` slice can't build `GlobalControllerAdvice` (no `IUsersService` mock).
- `MasterCrawlerApplicationTests.contextLoads` needs a live MySQL and `application.properties`, so it can't pass in a clean checkout.

---

## 7. Problems found, by severity

### 🔴 Critical
1. **Anyone can reset any account's password.** `POST /admin/change/password/save` is public and only needs an email, with no old password or token (`AuthenticationController.java:70`). That means full takeover of any Admin.
2. **Anyone can create an Admin account.** `/admin/signup/**` is public, so `POST /admin/signup/save` is too. It binds the whole `Users` object from the form, including `userTypeId` (`AuthenticationController.java:43`). The "secret" signup URL only hides the form, not the endpoint.
3. **Live secrets are in git history on `origin/main`.** `application.properties` was committed (`22765d9` … `b09e9cf`) before being deleted in `07805a4`. It held the Gemini key, the Whisper key, all X API/OAuth credentials and the encryption key. Deleting the file did not remove them from history, so **rotate every one of them.**
4. **A hardcoded tinn.ir API key** is in source: `AasaamCmsUploader.java:30`.

### 🟠 High
5. **Arbitrary file delete.** `DELETE /api/delete/audio?filename=../../…` does `new File("news/voices/" + filename).delete()` with no path check (`ConvertToTextRestController.java:95`). Any logged-in user, including Ai, can use it.
6. **`@PreAuthorize` does nothing.** There is no `@EnableMethodSecurity`, so `/api/socials/admin/*` (global bots, delete accounts) is open to every logged-in user, including Ai.
7. **Debug endpoints are live**: `/api/socials/debug/accounts`, `DELETE /api/socials/debug/clear-x-accounts`, `/api/socials/debug/x-token-analysis`.
8. **CSRF is disabled** app-wide while using cookie session auth.
9. **Weak encryption**: `CryptoUtil` uses `AES` (ECB mode), the default key is `MySecretKey12345`, and the key is truncated to 16 bytes.

### 🟡 Medium (bugs)
10. **Crawled news is always saved as user #1** (`ChapChinService.java:102`). Ai users who crawl never see their own crawled drafts, which defeats the recent "Ai user filter". It also assumes NewsType #3 exists.
11. **Social posts link to `https://yoursite.com/news/{id}`**, a placeholder left in `SocialPublisherService.java:277`.
12. **`Users.userTypeId` has `cascade = ALL` on a `@ManyToOne`.** Deleting a user can try to delete the shared "Admin"/"User"/"Ai" type row.
13. **RSS pollers are global singletons.** If two people use them they share state, and all saved news goes to user #1 anyway. `getLogs()` always returns empty because `addLog` never appends.
14. **`processJob` is called on `this`**, so its `@Transactional` is ignored.
15. **Auditing always records `SYSTEM_OPERATOR`.**
16. **The add/edit news page makes a live call to tinn.ir** on every load, which slows the page or breaks it when the site is offline.

### ⚪ Housekeeping
17. Tracked in git: `node_modules/` (2,325 files, including a macOS-only binary), the `news/` uploads (25 MB of user content and videos) and 61 `.DS_Store` files. `.git` is 83 MB.
18. Dead code: AboutUs, CallUs and Links (entity, repo, service, JS), `SeleniumConfig`, 28 unused templates, theme demo assets.
19. The README is wrong about the DB (MySQL, not PostgreSQL), the package names (`configuration/`, `utility/`) and the LICENSE file. There is no `application.properties.example`, so a new checkout can't tell which keys it needs.
20. Many `System.out.println` calls (some log auth contexts) instead of SLF4J.

---

## 8. Config keys the app needs

`application.properties` must exist at `src/main/resources/` with at least:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<db>
spring.datasource.username=
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.servlet.multipart.max-file-size=
spring.servlet.multipart.max-request-size=

spring.ai.openai.chat.options.model=      # required
gemini.api.url=                           # required
gemini.api.key=                           # required (startup fails if empty)
gemini.api.max-output-tokens=4096         # optional
whisper.api.key=                          # required

app.encryption.key=                       # set it; default is insecure
x.bearer.token=                           # required
x.oauth.client-id=
x.oauth.client-secret=
x.oauth.redirect-uri=http://localhost:8081/socials/x/callback

selenium.chrome.headless=true             # optional (currently unused)
selenium.chrome.timeout=30                # optional (currently unused)
```
Chrome must be installed for the Selenium scrapers.
