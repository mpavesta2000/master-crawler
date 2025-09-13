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
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class AlMayadeenScraper {

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
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

            driver.get(cleanUrl);
            Thread.sleep(3000); // Additional wait for dynamic content

            // Parse the page content with Jsoup
            Document doc = Jsoup.parse(driver.getPageSource(), cleanUrl);

            // Title: Multiple selectors for Al Mayadeen
            Element titleElement = doc.selectFirst("div.details-white-box h1");
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.entry-title");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.post-title");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1.news-title");
            }
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1");
            }
            String title = titleElement != null ? titleElement.text().trim() : "";
            System.out.println("Extracted title: " + title);

            // Lead/Subtitle: Al Mayadeen specific patterns
            Element leadElement = doc.selectFirst("p.lg_para.summary");
            if (leadElement == null) {
                leadElement = doc.selectFirst("div.article-excerpt");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst("p.entry-summary");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst("div.news-excerpt");
            }
            if (leadElement == null) {
                leadElement = doc.selectFirst("p.lead");
            }
            String lead = leadElement != null ? leadElement.text().trim() : "";
            System.out.println("Extracted lead: " + lead);

            // Body content: Al Mayadeen with comprehensive fallbacks
            Element bodyElement = doc.selectFirst("div.p-content");
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("div.entry-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("div.post-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("div.news-content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("div.content");
            }
            if (bodyElement == null) {
                bodyElement = doc.selectFirst("article");
            }

            String body = "";
            if (bodyElement != null) {
                // Content cleaning (Al Mayadeen specific)
                bodyElement.select("script").remove();
                bodyElement.select("style").remove();
                bodyElement.select("nav").remove();
                bodyElement.select("header").remove();
                bodyElement.select("footer").remove();
                bodyElement.select(".advertisement").remove();
                bodyElement.select(".ads").remove();
                bodyElement.select(".social-share").remove();
                bodyElement.select(".related-articles").remove();
                bodyElement.select(".author-info").remove();
                bodyElement.select(".tags").remove();
                bodyElement.select(".comments").remove();
                bodyElement.select(".sidebar").remove();
                
                // Comprehensive Twitter widget removal
                bodyElement.select("iframe").remove();
                bodyElement.select("p iframe").remove();
                bodyElement.select("#twitter-widget-0").remove();
                bodyElement.select("div.twitter-tweet").remove();
                bodyElement.select("div.twitter-tweet-rendered").remove();
                bodyElement.select(".twitter-tweet.twitter-tweet-rendered").remove();
                bodyElement.select("blockquote.twitter-tweet").remove();
                bodyElement.select("[class*='twitter']").remove();
                bodyElement.select("[id*='twitter']").remove();
                bodyElement.select("div[data-twitter-extracted-i1]").remove();
                bodyElement.select("figure").remove();
                bodyElement.select("div.dual-related").remove();
                bodyElement.select("iframe").remove();
                bodyElement.select("p iframe").remove();
                bodyElement.select("#twitter-widget-0").remove();
                //REMOVE TWITTER WIDGET
                bodyElement.select("div.twitter-tweet.twitter-tweet-rendered html").remove();

                // Remove all hyperlinks (keeping the text)
                bodyElement.select("a").unwrap();

                // Fix image sources - Al Mayadeen specific patterns
                Elements images = bodyElement.select("img");
                for (Element img : images) {
                    String dataSrc = img.attr("data-src");
                    String src = img.attr("src");
                    String finalUrl = "";
                    
                    if (!dataSrc.isEmpty()) {
                        finalUrl = dataSrc;
                    } else if (!src.isEmpty()) {
                        finalUrl = src;
                    }
                    
                    if (!finalUrl.isEmpty()) {
                        if (finalUrl.startsWith("//")) {
                            img.attr("src", "https:" + finalUrl);
                        } else if (finalUrl.startsWith("http://") || finalUrl.startsWith("https://")) {
                            img.attr("src", finalUrl);
                        } else {
                            // Convert relative URLs for Al Mayadeen
                            String baseUrl = "https://www.almayadeen.net/";
                            img.attr("src", baseUrl + finalUrl);
                        }
                    }
                }

                body = bodyElement.text().trim();
                System.out.println("Extracted body: " + body.substring(0, Math.min(body.length(), 200)) + "...");
            }

            // Cover image: Try multiple selectors for Al Mayadeen
            Element coverImgElement = doc.selectFirst("meta[property=og:image]");
            String cover = "";
            if (coverImgElement != null) {
                cover = coverImgElement.attr("content");
            } else {
                // Primary selector: Al Mayadeen main image (will need F12 refinement)
                coverImgElement = doc.selectFirst(".article-image img");
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".featured-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".entry-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".post-image img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst("figure img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst(".media img");
                }
                if (coverImgElement == null) {
                    coverImgElement = doc.selectFirst("img");
                }
                if (coverImgElement != null) {
                    String imgSrc = coverImgElement.attr("src");
                    String dataSrc = coverImgElement.attr("data-src");
                    
                    // Handle Al Mayadeen's image loading patterns
                    if (!dataSrc.isEmpty()) {
                        // Convert relative URLs to absolute
                        if (dataSrc.startsWith("//")) {
                            cover = "https:" + dataSrc;
                        } else if (dataSrc.startsWith("http://") || dataSrc.startsWith("https://")) {
                            cover = dataSrc;
                        } else {
                            cover = "https://www.almayadeen.net/" + dataSrc;
                        }
                    } else if (!imgSrc.isEmpty()) {
                        // Convert relative URLs to absolute
                        if (imgSrc.startsWith("//")) {
                            cover = "https:" + imgSrc;
                        } else if (imgSrc.startsWith("http://") || imgSrc.startsWith("https://")) {
                            cover = imgSrc;
                        } else {
                            cover = "https://www.almayadeen.net/" + imgSrc;
                        }
                    }
                }
            }

            // Clean content and build JSON response
            if (title.isEmpty() || body.isEmpty()) {
                System.out.println("Missing title or body content");
                return createEmptyJsonResponse();
            }

            // Clean text content
            body = body.replace("اقرأ أيضاً", "");
            body = body.replace("شاهد أيضاً", "");
            body = body.replace("مواضيع ذات صلة", "");
            body = body.replace("المزيد", "");
            
            // Remove Twitter content patterns from text
            body = body.replaceAll("—\\s*[A-Za-z\\s]+\\s*\\(@[A-Za-z0-9_]+\\).*?\\d{4}", "");
            body = body.replaceAll("@[A-Za-z0-9_]+", ""); // Remove Twitter handles
            body = body.replaceAll(".*twitter\\.com.*", ""); // Remove Twitter URLs
            
            // General pattern to remove embedded tweets (any text ending with — Name (@handle) Date)
            body = body.replaceAll("[^\\n]*—\\s*[A-Za-z\\s]+\\s*\\(@[A-Za-z0-9_]+\\)\\s*[A-Za-z]+\\s*\\d+,\\s*\\d{4}[^\\n]*", "");
            
            // Clean up multiple spaces and newlines

            // Build JSON response
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
            System.err.println("Error scraping Al Mayadeen: " + e.getMessage());
            return createEmptyJsonResponse();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private JsonNode createEmptyJsonResponse() {
        try {
            ObjectNode emptyNode = objectMapper.createObjectNode();
            emptyNode.put("title", "");
            emptyNode.put("lead", "");
            emptyNode.put("body", "");
            emptyNode.put("cover", "");
            emptyNode.put("url", "");
            emptyNode.put("local_cover", "");
            emptyNode.put("fa_title", "");
            emptyNode.put("fa_lead", "");
            emptyNode.put("fa_body", "");
            return new JsonNode(emptyNode.toString());
        } catch (Exception e) {
            return null;
        }
    }
} 