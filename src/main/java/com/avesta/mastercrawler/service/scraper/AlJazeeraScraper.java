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
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class AlJazeeraScraper {

    private final ObjectMapper objectMapper;
    private final GeminiService geminiService;

    public JsonNode scrape(ScrapeRequest scrapeRequest) {
        WebDriver driver = null;
        try {
            String cleanUrl = scrapeRequest.getUrl().trim();

            // Setup Chrome options for headless browsing - Same anti-detection as Alarabiya
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

            // Set user agent to avoid detection
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

            // Initialize WebDriver
            driver = new ChromeDriver(options);

            // Set increased timeouts for Al Jazeera
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

            // Navigate to the URL with retry logic
            System.out.println("Attempting to load Al Jazeera URL: " + cleanUrl);
            driver.get(cleanUrl);
            System.out.println("Page loaded successfully");

            // Wait for page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            Thread.sleep(2000); // Reduced wait time

            // Get page source and parse with Jsoup
            String htmlContent = driver.getPageSource();
            Document doc = Jsoup.parse(htmlContent, cleanUrl);

            // Title: multiple selectors for Al Jazeera
            Element titleElement = doc.selectFirst("h1");
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.headingInfo_title");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst(".article-title");
            }
            String title = titleElement != null ? titleElement.text() : "";
            System.out.println("Extracted title: " + (title.isEmpty() ? "NOT FOUND" : title.substring(0, Math.min(50, title.length())) + "..."));

            // Lead/Subtitle: Al Jazeera specific selector found via F12
            Element leadElement = doc.selectFirst("h2.headingInfo_subtitle");
            if (leadElement == null) {
                leadElement = doc.selectFirst("p.body-1.paragraph");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst(".article-summary");
            }
            String lead = leadElement != null ? leadElement.text() : "";
            System.out.println("Extracted lead: " + (lead.isEmpty() ? "NOT FOUND" : lead.substring(0, Math.min(50, lead.length())) + "..."));

            // Body content: Al Jazeera with fallbacks
            Element bodyElement = doc.selectFirst("div.wysiwyg.wysiwyg--all-content");
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".article-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".content");
            }

            
            String body = "";
            if (bodyElement != null) {
                // Remove unwanted elements (similar to Alarabiya pattern)
                bodyElement.select(".advertisement").remove();
                bodyElement.select(".ads").remove();
                bodyElement.select(".related-articles").remove();
                bodyElement.select(".social-share").remove();
                bodyElement.select(".comments").remove();
                bodyElement.select("script").remove();
                bodyElement.select("style").remove();
                bodyElement.select("div.video-player-facade-container").remove();
                bodyElement.select("span.heading-anchor-button").remove();
                bodyElement.select("figure.wp-caption.aligncenter").remove();
                bodyElement.select("iframe").remove();
                //REMOVE READ MORE
                bodyElement.select("div.more-on").remove();

                // Remove all hyperlinks (keeping the text)
                bodyElement.select("a").unwrap();

                // Fix image sources - Al Jazeera specific patterns
                Elements images = bodyElement.select("img");
                for (Element img : images) {
                    String dataSrc = img.attr("data-src");
                    String src = img.attr("src");
                    if (!dataSrc.isEmpty()) {
                        img.attr("src", dataSrc);
                    } else if (!src.isEmpty() && src.startsWith("//")) {
                        img.attr("src", "https:" + src);
                    }
                }

                // Get HTML content
                body = bodyElement.html().trim();

                // Clean Al Jazeera specific unwanted text
                body = body.replace("إعلان", "");
                body = body.replace("تابعوا آخر أخبارنا المحلية والرياضية وأخبار السياسة والاقتصاد عبر Google news", "");
            }
            
            System.out.println("Extracted body: " + (body.isEmpty() ? "NOT FOUND" : body.length() + " characters"));

            // Validate that we have at least some content
            if (title.isEmpty() && lead.isEmpty() && body.isEmpty()) {
                System.out.println("ERROR: No content extracted from Al Jazeera page - all fields empty");
                return createEmptyJsonResponse();
            }

            // Cover image
            Element coverImgElement = doc.selectFirst("meta[property=og:image]");
            String cover = "";
            if (coverImgElement != null) {
                cover = coverImgElement.attr("content");
            } else {
                coverImgElement = doc.selectFirst(".article-image img");
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".featured-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst("figure img");
                }
                if (coverImgElement != null) {
                    cover = coverImgElement.absUrl("src");
                }
            }

            // Build response using same format as Alarabiya
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.putNull("headline");
            jsonNode.put("title", geminiService.translateToFarsi(title));
            jsonNode.put("lead", geminiService.translateToFarsi(lead));
            jsonNode.put("cover", cover);
            jsonNode.putNull("video");
            jsonNode.put("content", geminiService.translateToFarsi(body));
            jsonNode.put("slug", title);
            jsonNode.put("local_cover", "");

            String jsonString = objectMapper.writeValueAsString(jsonNode);
            return new JsonNode(jsonString);

        } catch (Exception e) {
            System.out.println("ERROR in Al Jazeera scraper: " + e.getMessage());
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