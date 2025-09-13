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
public class BBCArabicScraper {

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

            // Set user agent to avoid detection
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

            // Initialize WebDriver
            driver = new ChromeDriver(options);

            // Set timeouts for BBC Arabic
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

            // Navigate to the URL
            System.out.println("Attempting to load BBC Arabic URL: " + cleanUrl);
            driver.get(cleanUrl);
            System.out.println("BBC Arabic page loaded successfully");

            // Wait for page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            Thread.sleep(2000); // Wait for dynamic content

            // Get page source and parse with Jsoup
            String htmlContent = driver.getPageSource();
            Document doc = Jsoup.parse(htmlContent, cleanUrl);

            // Title: Try multiple selectors for BBC Arabic (need F12 inspection)
            Element titleElement = doc.selectFirst("div.bbc-1pfktyq.ebmt73l0");
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.story-headline");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.article-headline");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst("[data-testid='headline']");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst(".headline");
            }
            String title = titleElement != null ? titleElement.text() : "";
            System.out.println("Extracted title: " + (title.isEmpty() ? "NOT FOUND" : title.substring(0, Math.min(50, title.length())) + "..."));

            // Lead/Subtitle: BBC Arabic specific patterns (need F12 inspection)
            Element leadElement = doc.selectFirst("p.story-intro");
            if (leadElement == null) {
                leadElement = doc.selectFirst("p.article-intro");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst(".story-summary");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst("[data-testid='summary']");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst("h2");
            }
            String lead = leadElement != null ? leadElement.text() : "";
            System.out.println("Extracted lead: " + (lead.isEmpty() ? "NOT FOUND" : lead.substring(0, Math.min(50, lead.length())) + "..."));

            // Body content: BBC Arabic with comprehensive fallbacks (need F12 inspection)
            Element bodyElement = doc.selectFirst("main.bbc-1i6u33");
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".story-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".article-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("article .text");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst(".post-content");
            }
            
            String body = "";
            if (bodyElement != null) {
                // Remove unwanted elements (BBC specific)
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
                bodyElement.select("section.bbc-1o3v5ug").remove();
                bodyElement.select("h1.article-heading.bbc-1w6gp7v.e1p3vdyi0").remove();
                bodyElement.select("img").remove();
                // IDK don't touch this it will ruin the page (don't put the dot)
                bodyElement.select("div.bbc-4wucq3 ebmt73l0").remove();
                //REMOVE THE MORE INLINE UNRELATED NEWS
                bodyElement.select("p.bbc-lafk41").remove();
                bodyElement.select("div.bbc-e78w4i.ejs9cgv1").remove();
                //REMOVE INLINE PICS
                bodyElement.select("figure.bbc-1qn0xuy").remove();
                //REMOVE HIDDEN ELEMENTS
                bodyElement.select("div.bbc-7zw6iy.e10dhiml2").remove();
                //REMOVE PODCAST PROMO
                bodyElement.select("div.bbc-32l10g.e1rfboeq7").remove();
                bodyElement.select("figure.media-container.bbc-1hu5uc3").remove();

                //REMOVE THE AUTHOR AND DATE
                bodyElement.select("div.bbc-1rvtlej").remove();

                // Remove all hyperlinks (keeping the text)
                bodyElement.select("a").unwrap();

                // Fix image sources - BBC specific patterns
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

                // Clean BBC Arabic specific unwanted text
                body = body.replace("مواضيع قد تهمك أيضا", "");
                body = body.replace("قد يهمك أيضاً", "");
                body = body.replace("Copyright", "");
            }
            
            System.out.println("Extracted body: " + (body.isEmpty() ? "NOT FOUND" : body.length() + " characters"));

            // Validate that we have at least some content
            if (title.isEmpty() && lead.isEmpty() && body.isEmpty()) {
                System.out.println("ERROR: No content extracted from BBC Arabic page - all fields empty");
                return null;
            }

            // Cover image: Try multiple selectors for BBC Arabic
            Element coverImgElement = doc.selectFirst("meta[property=og:image]");
            String cover = "";
            if (coverImgElement != null) {
                cover = coverImgElement.attr("content");
            } else {
                coverImgElement = doc.selectFirst(".story-image img");
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".article-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".featured-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst("figure img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".media img");
                }
                if (coverImgElement != null) {
                    cover = coverImgElement.absUrl("src");
                }
            }

            // Build response using same format as other scrapers
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
            System.out.println("ERROR in BBC Arabic scraper: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Always close the driver
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // private JsonNode createEmptyJsonResponse() {
    //     try {
    //         ObjectNode jsonNode = objectMapper.createObjectNode();
    //         jsonNode.putNull("headline");
    //         jsonNode.put("title", "");
    //         jsonNode.put("lead", "");
    //         jsonNode.put("cover", "");
    //         jsonNode.putNull("video");
    //         jsonNode.put("content", "");
    //         jsonNode.put("slug", "");
    //         jsonNode.put("local_cover", "");

    //         String jsonString = objectMapper.writeValueAsString(jsonNode);
    //         return new JsonNode(jsonString);
    //     } catch (Exception e) {
    //         System.out.println("Error creating empty JSON response: " + e.getMessage());
    //         return null;
    //     }
    // }
} 