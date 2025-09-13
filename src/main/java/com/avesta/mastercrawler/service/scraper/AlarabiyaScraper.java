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
public class AlarabiyaScraper {

    private final ObjectMapper objectMapper;
    private final GeminiService geminiService;

    public JsonNode scrape(ScrapeRequest scrapeRequest) {
        WebDriver driver = null;
        try {
            String cleanUrl = scrapeRequest.getUrl().trim();

            // Setup Chrome options for headless browsing
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

            // Set timeouts
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Navigate to the URL
            driver.get(cleanUrl);

            // Wait for page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            Thread.sleep(3000); // Additional wait for dynamic content

            // Get page source and parse with Jsoup
            String htmlContent = driver.getPageSource();
            Document doc = Jsoup.parse(htmlContent, cleanUrl);

            // Title: span with class font_bule
            Element titleElement = doc.selectFirst("h1.headingInfo_title");
            String title = titleElement != null ? titleElement.text() : "";

            // Lead is empty per your instruction
            Element leadElement = doc.selectFirst("h2.headingInfo_subtitle");
            String lead = leadElement != null ? leadElement.text() : "";

            // Select the body element
            Element bodyElement = doc.selectFirst("div#body-text");
            String body = "";

            if (bodyElement != null) {
                // Remove unwanted elements
                bodyElement.select(".feed-card").remove();
                bodyElement.select("div.video-player").remove();

                // Remove all hyperlinks (keeping the text)
                bodyElement.select("a").unwrap();

                // Fix image sources
                Elements images = bodyElement.select("img");
                for (Element img : images) {
                    String dataSrcDesktop = img.attr("data-src-desktop");
                    if (!dataSrcDesktop.isEmpty()) {
                        img.attr("src", dataSrcDesktop);
                    }
                }

                // Get HTML content
                body = bodyElement.html().trim();

                // Remove specific strings
                body = body.replace("مادة إعلانية", "");
            }


            // Cover: Try to get first img inside div.info or fallback empty string
            Element coverImgElement = doc.selectFirst("div.article-teaser img");
            String cover = "";
            if (coverImgElement != null) {
                cover = coverImgElement.absUrl("src");
            }

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
            e.printStackTrace();
            return null;
        } finally {
            // Always close the driver
            if (driver != null) {
                driver.quit();
            }
        }
    }
}