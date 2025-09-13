package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.SiteSetting;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.service.ISiteSettingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class NewsOrderRestController {

    private final INewsService newsService;
    private final ISiteSettingService siteSettingService;


    @PostMapping("/news/find")
    public ResponseEntity<Map<String, Object>> findNews(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String section = request.get("section");

        try {
            switch (section) {
                case "breakingNews":
                    return handleBreakingNews(response);
                case "newsHeadline":
                    return handleNewsHeadline(response);
                case "showMostViewed":
                    return handleMostViewed(response);
                case "slider":
                    return handleSlider(response);
                default:
                    response.put("message", "Invalid section type");
                    return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("message", "Error processing request: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleBreakingNews(Map<String, Object> response) {
        List<Integer> breakingNewsSort = siteSettingService.getBreakingNewsIds();
        List<News> breakingNewsList = newsService.getBreakingNews();

        List<Map<String, Object>> simplifiedNewsList = breakingNewsList.stream().map(this::simplifyNews).toList();

        response.put("breakingNewsSort", breakingNewsSort);
        response.put("breakingNewsList", simplifiedNewsList);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> handleNewsHeadline(Map<String, Object> response) {
        List<Integer> newsHeadlineSort = siteSettingService.getNewsHeadlineIds();
        List<News> newsHeadlineList = newsService.getNewsHeadline();

        List<Map<String, Object>> simplifiedNewsList = newsHeadlineList.stream().map(this::simplifyNews).toList();

        response.put("newsHeadlineSort", newsHeadlineSort);
        response.put("newsHeadlineList", simplifiedNewsList);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> handleMostViewed(Map<String, Object> response) {
        List<Integer> showMostViewedSort = siteSettingService.getMostViewedIds();
        List<News> showMostViewedList = newsService.getMostViewed();

        List<Map<String, Object>> simplifiedNewsList = showMostViewedList.stream().map(this::simplifyNews).toList();

        response.put("showMostViewedSort", showMostViewedSort);
        response.put("showMostViewedList", simplifiedNewsList);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> handleSlider(Map<String, Object> response) {
        List<Integer> sliderSort = siteSettingService.getSliderIds();
        List<News> sliderList = newsService.getSliderNews();

        List<Map<String, Object>> simplifiedNewsList = sliderList.stream().map(this::simplifyNews).toList();

        response.put("sliderSort", sliderSort);
        response.put("sliderList", simplifiedNewsList);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> simplifyNews(News news) {
        Map<String, Object> newsMap = new HashMap<>();
        newsMap.put("id", news.getId());
        newsMap.put("title", news.getTitle());
        return newsMap;
    }

    // Breaking News endpoints
    @PostMapping("/news/breakingNews/save")
    public ResponseEntity<Map<String, String>> saveBreakingNews(@RequestBody Map<String, List<Integer>> request) {
        return saveNewsToSection(request.get("breakingNewsIds"), "breakingNews", "اخبار فوری");
    }

    @PostMapping("/news/breakingNews/order/save")
    public ResponseEntity<Map<String, String>> saveBreakingNewsOrder(@RequestBody Map<String, List<Integer>> request) {
        return saveOrderToSection(request.get("order"), "breakingNews", "اخبار فوری");
    }

    // News Headlines endpoints
    @PostMapping("/news/newsHeadline/save")
    public ResponseEntity<Map<String, String>> saveNewsHeadline(@RequestBody Map<String, List<Integer>> request) {
        return saveNewsToSection(request.get("newsHeadlineIds"), "newsHeadline", "سرخط خبرها");
    }

    @PostMapping("/news/newsHeadline/order/save")
    public ResponseEntity<Map<String, String>> saveNewsHeadlineOrder(@RequestBody Map<String, List<Integer>> request) {
        return saveOrderToSection(request.get("order"), "newsHeadline", "سرخط خبرها");
    }

    // Most Viewed endpoints
    @PostMapping("/news/showMostViewed/save")
    public ResponseEntity<Map<String, String>> saveMostViewed(@RequestBody Map<String, List<Integer>> request) {
        return saveNewsToSection(request.get("showMostViewedIds"), "showMostViewed", "پربیننده ترین ها");
    }

    @PostMapping("/news/showMostViewed/order/save")
    public ResponseEntity<Map<String, String>> saveMostViewedOrder(@RequestBody Map<String, List<Integer>> request) {
        return saveOrderToSection(request.get("order"), "showMostViewed", "پربیننده ترین ها");
    }

    // Slider endpoints
    @PostMapping("/news/slider/save")
    public ResponseEntity<Map<String, String>> saveSlider(@RequestBody Map<String, List<Integer>> request) {
        return saveNewsToSection(request.get("sliderIds"), "slider", "اسلایدر");
    }

    @PostMapping("/news/slider/order/save")
    public ResponseEntity<Map<String, String>> saveSliderOrder(@RequestBody Map<String, List<Integer>> request) {
        return saveOrderToSection(request.get("order"), "slider", "اسلایدر");
    }

    // Generic delete endpoint for all sections
    @PostMapping("/news/delete/{section}/{newsId}")
    public ResponseEntity<String> deleteNews(@PathVariable("section") String section, @PathVariable("newsId") Integer newsId) {
        Optional<SiteSetting> siteSettingOpt = siteSettingService.findById(1);

        if (siteSettingOpt.isPresent()) {
            SiteSetting siteSetting = siteSettingOpt.get();
            List<Integer> newsList = getNewsListBySection(siteSetting, section);

            if (newsList != null && newsList.contains(newsId)) {
                newsList.remove(newsId);
                setNewsListBySection(siteSetting, section, newsList);
                siteSettingService.save(siteSetting);

                return ResponseEntity.ok("خبر با موفقیت حذف شد.");
            } else {
                return ResponseEntity.badRequest().body("خبر مورد نظر یافت نشد.");
            }
        } else {
            return ResponseEntity.status(500).body("خطای داخلی سرور: تنظیمات سایت یافت نشد.");
        }
    }

    private ResponseEntity<Map<String, String>> saveNewsToSection(List<Integer> newsIds, String section, String sectionName) {
        if (newsIds == null || newsIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "هیچ خبری انتخاب نشد."));
        }

        Optional<SiteSetting> siteSetting = siteSettingService.findById(1);
        if (siteSetting.isPresent()) {
            List<Integer> oldNews = getNewsListBySection(siteSetting.get(), section);
            if (oldNews == null) {
                oldNews = new ArrayList<>();
            }

            Set<Integer> updatedNews = new LinkedHashSet<>(oldNews);
            updatedNews.addAll(newsIds);

            setNewsListBySection(siteSetting.get(), section, new ArrayList<>(updatedNews));
            siteSettingService.save(siteSetting.get());
        } else {
            System.out.println("Fatal error: No site setting available in database");
            return ResponseEntity.status(500).body(Map.of("message", "خطای داخلی سرور"));
        }

        return ResponseEntity.ok(Map.of("message", sectionName + " با موفقیت ذخیره شدند."));
    }

    private ResponseEntity<Map<String, String>> saveOrderToSection(List<Integer> order, String section, String sectionName) {
        if (order == null || order.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "ترتیب خالی است."));
        }

        Optional<SiteSetting> siteSettingOpt = siteSettingService.findById(1);
        if (siteSettingOpt.isPresent()) {
            setNewsListBySection(siteSettingOpt.get(), section, order);
            siteSettingService.save(siteSettingOpt.get());
        } else {
            System.out.println("Fatal error: No site setting available in database");
            return ResponseEntity.status(500).body(Map.of("message", "خطای داخلی سرور"));
        }

        return ResponseEntity.ok(Map.of("message", "ترتیب " + sectionName + " با موفقیت ذخیره شد."));
    }

    private List<Integer> getNewsListBySection(SiteSetting siteSetting, String section) {
        switch (section) {
            case "breakingNews":
                return siteSetting.getBreakingNews();
            case "newsHeadline":
                return siteSetting.getNewsHeadline();
            case "showMostViewed":
                return siteSetting.getShowMostViewed();
            case "slider":
                return siteSetting.getSlider();
            default:
                return null;
        }
    }

    private void setNewsListBySection(SiteSetting siteSetting, String section, List<Integer> newsList) {
        switch (section) {
            case "breakingNews":
                siteSetting.setBreakingNews(newsList);
                break;
            case "newsHeadline":
                siteSetting.setNewsHeadline(newsList);
                break;
            case "showMostViewed":
                siteSetting.setShowMostViewed(newsList);
                break;
            case "slider":
                siteSetting.setSlider(newsList);
                break;
        }
    }
}