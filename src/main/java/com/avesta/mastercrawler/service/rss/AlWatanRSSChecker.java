package com.avesta.mastercrawler.service.rss;

import com.avesta.mastercrawler.service.scraper.ChapChinService;
import kong.unirest.JsonNode;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AlWatanRSSChecker {

    private final ChapChinService chapChinService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String RSS_URL = "https://www.al-watan.com/rssFeed/0";
    private static final long INTERVAL_MINUTES = 1;
    private static final int DEFAULT_MAX_NEWS = 5;

    private String lastLinkText = "";
    private List<String> logs = new ArrayList<>();

    private ScheduledExecutorService scheduler;
    private boolean enabled = false;
    private int newsCounter = 0;
    private int maxNews = DEFAULT_MAX_NEWS;

    @Autowired
    public AlWatanRSSChecker(ChapChinService chapChinService, SimpMessagingTemplate messagingTemplate) {
        this.chapChinService = chapChinService;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void startChecking() {
        if (!enabled) {
            addLog("RSS الوطن شروع به کار کرد.\n");
            enabled = true;
            newsCounter = 0;
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::checkForNewNews, 0, INTERVAL_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void stopChecking() {
        if (enabled) {
            addLog("RSS الوطن متوقف شد.\n");
            enabled = false;
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }
    }

    public void checkForNewNews() {
        try {
            if (newsCounter >= maxNews) {
                addLog("تعداد مجاز اخبار الوطن به پایان رسید: " + newsCounter + "/" + maxNews + "\n");
                stopChecking();
                return;
            }

            Document doc = Jsoup.connect(RSS_URL).get();
            Elements items = doc.select("item");

            if (!items.isEmpty()) {
                Element firstItem = items.first();
                String linkText = firstItem.select("guid").text();

                if (!linkText.equals(lastLinkText) && !linkText.equals("https://www.al-watan.com")) {
                    String title = firstItem.select("title").text();
                    String pubDate = firstItem.select("pubDate").text();

                    addLog("خبر جدید الوطن یافت شد: " + title + " (" + pubDate + ")\n");
                    addLog("در حال پردازش: " + linkText + "\n");

                    JsonNode response = chapChinService.chapChinResponse(linkText);
                    if (response != null) {
                        String result = chapChinService.chapChinSaveNews(response);
                        addLog("نتیجه ذخیره سازی: " + result + "\n");
                        newsCounter++;
                        addLog("تعداد اخبار پردازش شده الوطن: " + newsCounter + "/" + maxNews + "\n");
                    } else {
                        addLog("خطا در پردازش خبر الوطن\n");
                    }

                    lastLinkText = linkText;
                } else {
                    addLog("هیچ خبر جدیدی در الوطن یافت نشد.\n");
                }
            }
        } catch (Exception e) {
            addLog("خطا در بررسی RSS الوطن: " + e.getMessage() + "\n");
        }
    }

    private void addLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + message;
        logs.add(logMessage);

        // 100 ta ?
        if (logs.size() > 100) {
            logs.remove(0);
        }

        // ferestadan be websocket
        messagingTemplate.convertAndSend("/topic/logs", logMessage);
        System.out.print(logMessage);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setMaxNews(int maxNews) {
        this.maxNews = maxNews;
        addLog("حداکثر تعداد اخبار الوطن تنظیم شد: " + maxNews + "\n");
    }

    public int getMaxNews() {
        return maxNews;
    }

    public int getNewsCounter() {
        return newsCounter;
    }
} 