package com.avesta.mastercrawler.service.rss;

import com.avesta.mastercrawler.service.scraper.ChapChinService;
import kong.unirest.JsonNode;
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
public class BBCArabicRSSChecker {

    private final ChapChinService chapChinService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String RSS_URL = "https://feeds.bbci.co.uk/arabic/rss.xml";
    private static final long INTERVAL_MINUTES = 1;
    private static final int DEFAULT_MAX_NEWS = 5;

    private String lastLinkText = "";
    private List<String> logs = new ArrayList<>();

    private ScheduledExecutorService scheduler;
    private boolean enabled = false;
    private int newsCounter = 0;
    private int maxNews = DEFAULT_MAX_NEWS;

    @Autowired
    public BBCArabicRSSChecker(ChapChinService chapChinService, SimpMessagingTemplate messagingTemplate) {
        this.chapChinService = chapChinService;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void startChecking() {
        if (!enabled) {
            addLog("RSS بی بی سی عربی شروع به کار کرد.\n");
            enabled = true;
            newsCounter = 0;
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::checkForNewNews, 0, INTERVAL_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void stopChecking() {
        if (enabled) {
            addLog("RSS بی بی سی عربی متوقف شد.\n");
            enabled = false;
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setMaxNews(int maxNews) {
        if (enabled) {
            throw new IllegalStateException("نمی توانید تعداد اخبار را تغییر دهید وقتی RSS فعال است.\n");
        }
        this.maxNews = Math.max(1, Math.min(maxNews, 20));
        addLog("حداکثر تعداد اخبار تنظیم شد: " + this.maxNews + "\n");
    }

    public int getMaxNews() {
        return maxNews;
    }

    private void checkForNewNews() {
        try {
            if (newsCounter >= maxNews) {
                stopChecking();
                addLog("حداکثر تعداد اخبار دریافت شده است.\n");
                return;
            }

            Document document = Jsoup.connect(RSS_URL).get();
            Elements links = document.select("link");

            String firstLinkText = "";

            for (Element link : links) {
                String linkText = link.text();
                if (!linkText.equals("https://www.bbc.co.uk/arabic")) {
                    firstLinkText = linkText;
                    if (firstLinkText.equals(lastLinkText)) {
                        addLog("خبر جدیدی از سایت بی بی سی عربی یافت نشد.\n");
                    } else {
                        lastLinkText = firstLinkText;
                        newsCounter++;
                        addLog("خبر شماره " + newsCounter + ": خبر جدید از سایت بی بی سی عربی یافت شد: " + firstLinkText + "\n");
                        JsonNode response = chapChinService.chapChinResponse(firstLinkText);
                        addLog(chapChinService.chapChinSaveNews(response));

                        if (newsCounter >= maxNews) {
                            stopChecking();
                            addLog("حداکثر تعداد اخبار دریافت شده است.\n");
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            addLog(e.getMessage() + "\n");
        }
    }

    private void addLog(String message) {
        String log = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + message;
        messagingTemplate.convertAndSend("/topic/logs", log);
    }

    public List<String> getLogs() {
        return logs;
    }
} 