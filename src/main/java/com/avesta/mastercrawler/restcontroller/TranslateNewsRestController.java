package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.Category;
import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.NewsType;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.ICategoryService;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.service.INewsTypeService;
import com.avesta.mastercrawler.service.IUsersService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class TranslateNewsRestController {

    private final INewsService iNewsService;
    private final ChatLanguageModel chatLanguageModel;
    private final IUsersService iUsersService;
    private final INewsTypeService iNewsTypeService;
    private final ICategoryService iCategoryService;

    @PostMapping("/translate/news")
    public ResponseEntity<Map<String, String>> translateNews(
            @RequestParam(required = false) String newsId,
            @RequestParam(required = false) String language) {

        if (newsId == null || language == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing newsId or language"));
        }

        int parsedNewsId;
        try {
            parsedNewsId = Integer.parseInt(newsId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid newsId format"));
        }

        Optional<News> foundedNews = iNewsService.findById(parsedNewsId);
        if (foundedNews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "News not found"));
        }

        News news = foundedNews.get();
        Map<String, String> translatedContent = new HashMap<>();

        String prompt;
        switch (language) {
            case "en" -> prompt = "این متن را به انگلیسی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "fr" -> prompt = "این متن را به فرانسوی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "ar" -> prompt = "این متن را به عربی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "ur" -> prompt = "این متن را به اردو ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "es" -> prompt = "این متن را به اسپانیایی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "zh" -> prompt = "این متن را به چینی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "id" -> prompt = "این متن را به اندونزیایی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "bn" -> prompt = "این متن را به بنگالی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "ru" -> prompt = "این متن را به روسی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "ha" -> prompt = "این متن را به هوسا ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "sw" -> prompt = "این متن را به سواحیلی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "hi" -> prompt = "این متن را به هندی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "ps" -> prompt = "این متن را به پشتو ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "it" -> prompt = "این متن را به ایتالیایی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "az" -> prompt = "این متن را به آذربایجانی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "fil" -> prompt = "این متن را به فیلیپینی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "de" -> prompt = "این متن را به آلمانی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "pt" -> prompt = "این متن را به پرتغالی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "ms" -> prompt = "این متن را به مالایی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            case "tg" -> prompt = "این متن را به تاجیکی ترجمه کن و هیچ حرف اضافی دیگری نگو و فقط متن ترجمه شده را بده";
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "زبان شما پشتیبانی نمی شود."));
            }
        }

        if (news.getHeadline() != null && !news.getHeadline().isEmpty()) {
            translatedContent.put("headline", chatLanguageModel.chat(prompt + news.getHeadline()));
        }

        if (news.getTitle() != null && !news.getTitle().isEmpty()) {
            translatedContent.put("title", chatLanguageModel.chat(prompt + news.getTitle()));
        }

        if (news.getSubTitle() != null && !news.getSubTitle().isEmpty()) {
            translatedContent.put("subHeadline", chatLanguageModel.chat(prompt + news.getSubTitle()));
        }

        if (news.getLead() != null && !news.getLead().isEmpty()) {
            translatedContent.put("lead", chatLanguageModel.chat(prompt + news.getLead()));
        }

        if (news.getBody() != null && !news.getBody().isEmpty()) {
            translatedContent.put("body", chatLanguageModel.chat(prompt + news.getBody()));
        }

        return ResponseEntity.ok(translatedContent);
    }

    @PostMapping("/translate/save")
    public ResponseEntity<Map<String, String>> saveGeneratedNewsData(@RequestParam(required = false) String body, @RequestParam(required = false) String title, @RequestParam(required = false) String lead, @RequestParam(required = false) String headline, @RequestParam(required = false) String subheadline) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = iUsersService.findByEmail(authentication.getName()).orElseThrow(()->new UsernameNotFoundException("user not found."));
        NewsType newsType = iNewsTypeService.findById(3)
                .orElseThrow(() -> new IllegalArgumentException("NewsType with id 3 not found"));

        if (body == null || body.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "محتوا نمی‌تواند خالی باشد."));
        }

        try {
            Category defaultCategory = iCategoryService.defaultCategory();
            List<Category> updatedCategories = new ArrayList<>();
            updatedCategories.add(defaultCategory);

            News news = new News();
            news.setNewsTypeId(newsType);
            news.setBody(body);
            news.setUserId(user);
            news.setTitle(title);
            news.setLead(lead);
            news.setHeadline(headline);
            news.setSubTitle(subheadline);
            news.setStatus("Draft");
            news.setCategories(updatedCategories);

            iNewsService.save(news);

            return ResponseEntity.ok(Collections.singletonMap("message", "مقاله با موفقیت ذخیره شد."));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "خطایی در ذخیره مقاله رخ داد."));
        }
    }

}
