package com.avesta.mastercrawler.service.scraper;

import com.avesta.mastercrawler.dto.ScrapeRequest;
import com.avesta.mastercrawler.service.ai.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kong.unirest.JsonNode;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class AlWatanScraper {

    private final ObjectMapper objectMapper;
    private final GeminiService geminiService;

    public JsonNode scrape(ScrapeRequest scrapeRequest) {
        WebDriver driver = null;
        try {
            String cleanUrl = scrapeRequest.getUrl().trim();

            // Setup Chrome options for headless browsing - Same anti-detection as other scrapers
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-images");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--ignore-ssl-errors");
            options.addArguments("--ignore-certificate-errors-spki-list");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);
            
            // Avesta:EAGER page load strategy 
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            // Set user agent to avoid detection
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

            // Initialize WebDriver
            driver = new ChromeDriver(options);

            // Set timeouts for Al Watan
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

            // Navigate to the URL
            System.out.println("Attempting to load Al Watan URL: " + cleanUrl);
            driver.get(cleanUrl);
            System.out.println("Al Watan page loaded successfully");

            // Wait for page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            Thread.sleep(2000); // Wait for dynamic content

            // Get page source and parse with Jsoup
            String htmlContent = driver.getPageSource();
            Document doc = Jsoup.parse(htmlContent, cleanUrl);

            // Title: multiple selectors for Al Watan
            Element titleElement = doc.selectFirst("h1.article-title");
            System.out.println("Title element: " + titleElement);
            if (titleElement == null) {
                titleElement = doc.selectFirst(".article-title");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.news-title");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst(".article-header h1");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst(".news-header h1");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst(".title");
            }
            String title = titleElement != null ? titleElement.text() : "";
            System.out.println("Extracted title: " + (title.isEmpty() ? "NOT FOUND" : title.substring(0, Math.min(50, title.length())) + "..."));

            // Lead/Subtitle: Al Watan specific patterns
            Element leadElement = doc.selectFirst("p.article-lead");
            if (leadElement == null) {
                leadElement = doc.selectFirst("p.news-lead");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst(".article-summary");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst(".news-summary");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst("h2");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst(".lead");
            }
            String lead = leadElement != null ? leadElement.text() : "";
            System.out.println("Extracted lead: " + (lead.isEmpty() ? "NOT FOUND" : lead.substring(0, Math.min(50, lead.length())) + "..."));

            // Body content: Al Watan with comprehensive fallbacks
            Element bodyElement = doc.selectFirst("div.centerColumn");
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".news-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".article-body");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".news-body");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("article .text");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".post-content");
            }
            
            String body = "";
            if (bodyElement != null) {
                // Remove unwanted elements (Al Watan specific)
                bodyElement.select(".advertisement").remove();
                bodyElement.select(".ads").remove();
                bodyElement.select(".related-articles").remove();
                bodyElement.select(".social-share").remove();
                bodyElement.select(".comments").remove();
                bodyElement.select("script").remove();
                bodyElement.select("style").remove();
                bodyElement.select(".media-player").remove();
                bodyElement.select(".video-player").remove();
                bodyElement.select("figure.media").remove();
                bodyElement.select("iframe").remove();
                bodyElement.select(".promo").remove();
                bodyElement.select(".sidebar").remove();
                bodyElement.select(".widget").remove();

                // Al Watan specific unwanted elements
                bodyElement.select(".share-buttons").remove();
                bodyElement.select(".article-tags").remove();
                bodyElement.select(".author-info").remove();
                bodyElement.select(".related-news").remove();
                bodyElement.select("div.col-sm-6.col-lg-2").remove();
                bodyElement.select("div.row").remove();
                bodyElement.select("#imgSrc").remove();
                //REMOVE COLOUR CAPTION
                bodyElement.select("span.colorcaption").remove();

                // Remove ALL images from body content

                bodyElement.select("img").remove();
                bodyElement.select("div.lazy.owl-lazy.loaded").remove();
                bodyElement.select("figure").remove();
                bodyElement.select(".image").remove();
                
                // Remove all hyperlinks (keeping the text)
                bodyElement.select("a").unwrap();
                bodyElement.select("div.rightColumn").remove();
                bodyElement.select("div.endInfo").remove();

                // Get text content (no HTML with images to avoid processing errors)
                body = bodyElement.text().trim();

                // Clean Al Watan specific unwanted text
                body = body.replace("إعلان", "");
                body = body.replace("أخبار ذات صلة", "");
                body = body.replace("اقرأ أيضا", "");
                body = body.replace("للمزيد", "");
            }
            
            System.out.println("Extracted body: " + (body.isEmpty() ? "NOT FOUND" : body.length() + " characters"));

            // Validate that we have at least some content
            if (title.isEmpty() && lead.isEmpty() && body.isEmpty()) {
                System.out.println("ERROR: No content extracted from Al Watan page - all fields empty");
                return createEmptyJsonResponse();
            }

            // Cover image: Al Watan specific selectors with proper fallbacks
            String cover = "";
            
            // Try meta og:image first (most reliable)
            Element coverImgElement = doc.selectFirst("meta[property=og:image]");
            if (coverImgElement != null) {
                cover = coverImgElement.attr("content");
                System.out.println("Found cover image from og:image: " + cover);
            } else {
                // Try various Al Watan image selectors
                coverImgElement = doc.selectFirst("img.lazy.owl-lazy.loaded");
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".article-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".news-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst("div.relative.layout-ratio img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst("figure img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".featured-image img");
                }
                if (coverImgElement == null) {
                    // Last resort - any img in the article
                    coverImgElement = doc.selectFirst("img");
                }
                if (coverImgElement != null) {
                    String imgSrc = coverImgElement.attr("src");
                    String dataSrc = coverImgElement.attr("data-src");
                    
                    // Handle Al Watan's image loading patterns
                    if (!dataSrc.isEmpty()) {
                        // Convert relative URLs to absolute
                        if (dataSrc.startsWith("//")) {
                            cover = "https:" + dataSrc;
                        } else if (dataSrc.startsWith("http://") || dataSrc.startsWith("https://")) {
                            cover = dataSrc;
                        } else {
                            cover = "https://www.al-watan.com/" + dataSrc;
                        }
                        System.out.println("Found cover image from data-src: " + cover);
                    } else if (!imgSrc.isEmpty()) {
                        // Convert relative URLs to absolute
                        if (imgSrc.startsWith("//")) {
                            cover = "https:" + imgSrc;
                        } else if (imgSrc.startsWith("http://") || imgSrc.startsWith("https://")) {
                            cover = imgSrc;
                        } else {
                            cover = "https://www.al-watan.com/" + imgSrc;
                        }
                        System.out.println("Found cover image from src: " + cover);
                    }
                }
            }
            
            // Log if no cover image found (remove she age ezafie)
            if (cover.isEmpty()) {
                System.out.println("WARNING: No cover image found for Al Watan article");
            }

            // Build response using same format as other scrapers
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.putNull("headline");
            jsonNode.put("title", geminiService.translateToFarsi(title));
            jsonNode.put("lead", geminiService.translateToFarsi(lead));
            
            // Handle empty cover image - use null instead of empty string to prevent ImageDownloadUtil errors
            if (cover.isEmpty()) {
                jsonNode.putNull("cover");
                System.out.println("Setting cover to null due to empty URL");
            } else {
                jsonNode.put("cover", cover);
            }
            
            jsonNode.putNull("video");
            jsonNode.put("content", geminiService.translateToFarsi(body));
            jsonNode.put("slug", title);
            jsonNode.put("local_cover", "");

            String jsonString = objectMapper.writeValueAsString(jsonNode);
            return new JsonNode(jsonString);

        } catch (Exception e) {
            System.out.println("ERROR in Al Watan scraper: " + e.getMessage());
            e.printStackTrace();
            return createEmptyJsonResponse();
        } finally {
            // Always close the driver
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private JsonNode createEmptyJsonResponse() {
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.putNull("headline");
            jsonNode.put("title", "");
            jsonNode.put("lead", "");
            jsonNode.put("cover", "");
            jsonNode.putNull("video");
            jsonNode.put("content", "");
            jsonNode.put("slug", "");
            jsonNode.put("local_cover", "");

            String jsonString = objectMapper.writeValueAsString(jsonNode);
            return new JsonNode(jsonString);
        } catch (Exception e) {
            System.out.println("Error creating empty JSON response: " + e.getMessage());
            return null;
        }
    }
} 