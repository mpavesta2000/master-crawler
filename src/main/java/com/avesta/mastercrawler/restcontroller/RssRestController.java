package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.service.rss.AlWatanRSSChecker;
import com.avesta.mastercrawler.service.rss.BBCArabicRSSChecker;
import com.avesta.mastercrawler.service.rss.BornaRSSChecker;
import com.avesta.mastercrawler.service.rss.FararuRSSChecker;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class RssRestController {

    private final BornaRSSChecker bornaRSSChecker;
    private final FararuRSSChecker fararuRSSChecker;
    private final BBCArabicRSSChecker bbcArabicRSSChecker;
    private final AlWatanRSSChecker alWatanRSSChecker;

    @PostMapping("/borna-rss/toggle")
    public void toggleBornaRSS(@RequestBody Map<String, Boolean> request) {
        boolean enabled = request.get("enabled");
        if (enabled) {
            bornaRSSChecker.startChecking();
        } else {
            bornaRSSChecker.stopChecking();
        }
    }

    @PostMapping("/borna-rss/max-news")
    public ResponseEntity<?> setMaxNews(@RequestBody Map<String, Integer> request) {
        try {
            int maxNews = request.get("maxNews");
            bornaRSSChecker.setMaxNews(maxNews);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("title", "خطا");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/borna-rss/max-news")
    public int getMaxNews() {
        return bornaRSSChecker.getMaxNews();
    }

    @PostMapping("/fararu-rss/toggle")
    public void toggleFararuRSS(@RequestBody Map<String, Boolean> request) {
        boolean enabled = request.get("enabled");
        if (enabled) {
            fararuRSSChecker.startChecking();
        } else {
            fararuRSSChecker.stopChecking();
        }
    }

    @PostMapping("/fararu-rss/max-news")
    public ResponseEntity<?> setFararuMaxNews(@RequestBody Map<String, Integer> request) {
        try {
            int maxNews = request.get("maxNews");
            fararuRSSChecker.setMaxNews(maxNews);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("title", "خطا");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/fararu-rss/max-news")
    public int getFararuMaxNews() {
        return fararuRSSChecker.getMaxNews();
    }

    @PostMapping("/bbcarabic-rss/toggle")
    public void toggleBBCArabicRSS(@RequestBody Map<String, Boolean> request) {
        boolean enabled = request.get("enabled");
        if (enabled) {
            bbcArabicRSSChecker.startChecking();
        } else {
            bbcArabicRSSChecker.stopChecking();
        }
    }

    @PostMapping("/bbcarabic-rss/max-news")
    public ResponseEntity<?> setBBCArabicMaxNews(@RequestBody Map<String, Integer> request) {
        try {
            int maxNews = request.get("maxNews");
            bbcArabicRSSChecker.setMaxNews(maxNews);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("title", "خطا");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/bbcarabic-rss/max-news")
    public int getBBCArabicMaxNews() {
        return bbcArabicRSSChecker.getMaxNews();
    }

    @PostMapping("/alwatan-rss/toggle")
    public void toggleAlWatanRSS(@RequestBody Map<String, Boolean> request) {
        boolean enabled = request.get("enabled");
        if (enabled) {
            alWatanRSSChecker.startChecking();
        } else {
            alWatanRSSChecker.stopChecking();
        }
    }

    @PostMapping("/alwatan-rss/max-news")
    public ResponseEntity<Map<String, String>> setAlWatanMaxNews(@RequestBody Map<String, Integer> request) {
        try {
            int maxNews = request.get("maxNews");
            alWatanRSSChecker.setMaxNews(maxNews);

            Map<String, String> response = new HashMap<>();
            response.put("title", "موفقیت آمیز");
            response.put("message", "حداکثر تعداد اخبار الوطن با موفقیت تنظیم شد: " + maxNews);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("title", "خطا");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/alwatan-rss/max-news")
    public int getAlWatanMaxNews() {
        return alWatanRSSChecker.getMaxNews();
    }
}
