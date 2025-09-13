package com.avesta.mastercrawler.service.scraper;

import com.avesta.mastercrawler.dto.ScrapeRequest;
import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.service.*;
import com.avesta.mastercrawler.service.ai.GeminiService;
import com.avesta.mastercrawler.utility.ImageDownloadUtil;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@AllArgsConstructor
public class ChapChinService {

    private final ICategoryService iCategoryService;
    private final IUsersService usersService;
    private final INewsService iNewsService;
    private final INewsTypeService iNewsTypeService;
    private final IImagesService iImagesService;
    private final GeminiService geminiService;
    private final ITagsService iTagsService;
    private final AlarabiyaScraper alarabiyaScraper;
    private final AlJazeeraScraper alJazeeraScraper;
    private final BBCArabicScraper bbcArabicScraper;
    private final AlAhramScraper alAhramScraper;
    private final AlMayadeenScraper alMayadeenScraper;
    private final AlWatanScraper alWatanScraper;


    public JsonNode chapChinResponse(String url) {
        String baseUrl = "https://chapchin.net/api/pull/legacy";
        Map<String, String> urlMapper = new HashMap<>();
        urlMapper.put("www.alarabiya.net", "alarabiya");
        urlMapper.put("www.aljazeera.net", "aljazeera");
        urlMapper.put("www.bbc.com/arabic", "bbcarabic");
        urlMapper.put("ahram.org.eg", "alahram");
        urlMapper.put("gate.ahram.org.eg", "alahram");
        urlMapper.put("www.almayadeen.net", "almayadeen");
        urlMapper.put("www.al-watan.com", "alwatan");


        String key = "";
        for (String domain : urlMapper.keySet()) {
            if (url.contains(domain)) {
                key = urlMapper.get(domain);
                break;
            }
        }

        switch (key) {
            case "alarabiya":
                System.out.println("Using Alarabiya scraper");
                ScrapeRequest scrapeRequest = new ScrapeRequest(url);
                return alarabiyaScraper.scrape(scrapeRequest);
            case "aljazeera":
                System.out.println("Using Al Jazeera scraper");
                ScrapeRequest alJazeeraScrapeRequest = new ScrapeRequest(url);
                return alJazeeraScraper.scrape(alJazeeraScrapeRequest);
            case "bbcarabic":
                System.out.println("Using BBC Arabic scraper");
                ScrapeRequest bbcScrapeRequest = new ScrapeRequest(url);
                return bbcArabicScraper.scrape(bbcScrapeRequest);
            case "alahram":
                System.out.println("Using Al Ahram scraper");
                ScrapeRequest alAhramScrapeRequest = new ScrapeRequest(url);
                return alAhramScraper.scrape(alAhramScrapeRequest);
            case "almayadeen":
                System.out.println("Using Al Mayadeen scraper");
                ScrapeRequest alMayadeenScrapeRequest = new ScrapeRequest(url);
                return alMayadeenScraper.scrape(alMayadeenScrapeRequest);
            case "alwatan":
                System.out.println("Using Al Watan scraper");
                ScrapeRequest alWatanScrapeRequest = new ScrapeRequest(url);
                return alWatanScraper.scrape(alWatanScrapeRequest);
            default:
                System.out.println("Using default ChapChin API");
                HttpResponse<JsonNode> response = Unirest.post(baseUrl)
                        .field("url", url)
                        .asJson();
                return response.getBody();
        }
    }

    public String chapChinSaveNews(JsonNode response) throws IOException {
        // Check if response is null or empty
        if (response == null || response.getObject() == null) {
            return "خطا: پاسخ از سرویس خبر خالی یا معیوب است.";
        }
        Users user = usersService.findById(1)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        NewsType newsType = iNewsTypeService.findById(3)
                .orElseThrow(() -> new IllegalArgumentException("NewsType with id 3 not found"));

        Category defaultCategory = iCategoryService.defaultCategory();
        List<Category> updatedCategories = new ArrayList<>();
        updatedCategories.add(defaultCategory);

        // Validate that we have required content
        String rawTitle = response.getObject().getString("title");
        String rawContent = response.getObject().getString("content");
        
        if (rawTitle == null || rawTitle.trim().isEmpty() || rawContent == null || rawContent.trim().isEmpty()) {
            return "خطا: عنوان یا محتوای خبر خالی است. امکان ذخیره سازی وجود ندارد.";
        }

        News news = new News();
        news.setNewsTypeId(newsType);
        news.setUserId(user);
        String title = geminiService.modifyNewsParts(rawTitle, "title");
        String body = geminiService.modifyNewsParts(rawContent, "body");
        String lead = geminiService.modifyNewsParts(response.getObject().getString("lead"), "lead");
        String tags = geminiService.modifyNewsParts(response.getObject().getString("content"), "tag");
        String metaTags = geminiService.modifyNewsParts(response.getObject().getString("content"), "metaTag");

        String[] tagArray = tags.split("[,،]");
        List<String> tagList = Arrays.asList(tagArray);
        List<Tags> newsTags = new ArrayList<>();
        for (String tag : tagList) {
            Tags t = iTagsService.addTagIfNotExists(tag.trim());
            newsTags.add(t);
        }

        String metaTitle = geminiService.modifyNewsParts(response.getObject().getString("content"), "metaTitle");
        String metaDescription = geminiService.modifyNewsParts(response.getObject().getString("content"), "metaDescription");

        if(response.getObject().get("headline") != null) {
            String headline = geminiService.modifyNewsParts(response.getObject().getString("headline"), "headline");
            news.setHeadline(headline);
        }
        news.setTitle(title);
        news.setLead(lead);
        news.setTags(newsTags);
        news.setMetaTitle(metaTitle);
        news.setMetaDescription(metaDescription);
        news.setMetaKeywords(metaTags);
        news.setStatus("Draft");
        news.setCategories(updatedCategories);

        String updatedBody = ImageDownloadUtil.processImages(body, iImagesService);
        news.setBody(updatedBody);

        try {
            String uploadDir = "news/photos/";
            Files.createDirectories(Paths.get(uploadDir));
            String imageName = "news_image_" + System.currentTimeMillis() + ".jpg";
            Path imagePath = Paths.get(uploadDir, imageName);
            news.setMainImage(imageName);
            try (InputStream in = new URL(response.getObject().getString("cover")).openStream()) {
                Files.copy(in, imagePath);
                Images gallery = new Images(imageName, '/' + uploadDir + imageName);
                iImagesService.save(gallery);
            }
        } catch (IOException e) {
            throw new IOException("Error downloading or saving image: " + response.getObject().getString("cover"), e);
        }
        News savedNews = iNewsService.save(news);
        return "خبر :"+ savedNews.getTitle() + "با موفقیت در سایت آپلود شد";
    }
}
