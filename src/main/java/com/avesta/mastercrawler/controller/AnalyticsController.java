package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.service.IUserMonthlyReportService;
import com.avesta.mastercrawler.service.IUsersService;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.model.UserMonthlyReport;
import com.avesta.mastercrawler.model.News;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.OptionalDouble;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AnalyticsController {

    private final IUserMonthlyReportService iUserMonthlyReportService;
    private final IUsersService iUsersService;
    private final INewsService iNewsService;

    @GetMapping("/full-users/report")
    public String adminAnalytics(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = iUsersService.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("user not found."));

        // Get ALL users and their data for system-wide statistics
        List<Users> allUsers = iUsersService.findAll();
        
        // sum of data from all editorss
        long totalNewsAdded = 0;
        long totalEdits = 0; 
        long totalChapChinUsage = 0;
        long totalAIAssisted = 0;
        
        for (Users editor : allUsers) {
            // Skip admin users, only count editors
            if (editor.getUserTypeId().getUserTypeName().equals("Admin")) {
                continue;
            }
            
            List<News> editorNews = iNewsService.findByUserId(editor);
            List<UserMonthlyReport> editorReports = editor.getMonthlyReports();
            
            // Sum news count
            totalNewsAdded += editorNews.size();
            
            // Sum edits from monthly reports
            long editorEdits = editorReports.stream()
                .mapToLong(report -> report.getEditsCounter() != null ? report.getEditsCounter() : 0L)
                .sum();
            totalEdits += editorEdits;
            
            // Count ChapChin usage
            long chapChinCount = editorNews.stream()
                .mapToLong(news -> (news.getChapChin() != null && news.getChapChin()) ? 1 : 0)
                .sum();
            totalChapChinUsage += chapChinCount;
            
            // Count AI assisted articles
            long aiAssistedCount = editorNews.stream()
                .mapToLong(news -> (news.getChapChin() != null && news.getChapChin()) ? 1 : 0)
                .sum();
            totalAIAssisted += aiAssistedCount;
        }
        
        // Calculate AI percentage
        double aiPercentage = totalNewsAdded > 0 ? ((double) totalAIAssisted / totalNewsAdded) * 100 : 0;

        // Generate weekly data for the last 7 days (news1+news2+news3+...)
        LocalDate today = LocalDate.now();
        List<String> weekLabels = new ArrayList<>();
        List<Long> weeklyNewsData = new ArrayList<>();
        List<Long> weeklyEditsData = new ArrayList<>();
        List<Long> weeklyChapChinData = new ArrayList<>();
        
        // Persian day names
        String[] persianDays = {"شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"};
        
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            
            // Add Persian day name
            int dayOfWeek = targetDate.getDayOfWeek().getValue() % 7; // Convert to 0-6 range
            weekLabels.add(persianDays[dayOfWeek]);
            
            // Count news added on this day
            long dailyNewsCount = 0;
            long dailyEditsCount = 0;
            long dailyChapChinCount = 0;
            
            for (Users editor : allUsers) {
                if (editor.getUserTypeId().getUserTypeName().equals("Admin")) {
                    continue;
                }
                
                List<News> editorNews = iNewsService.findByUserId(editor);
                
                // Count news created on this day
                long newsCreatedToday = editorNews.stream()
                    .filter(news -> news.getCreatedAt() != null)
                    .filter(news -> news.getCreatedAt().toLocalDate().equals(targetDate))
                    .count();
                dailyNewsCount += newsCreatedToday;
                
                // where updated_at != created_at
                long newsEditedToday = editorNews.stream()
                    .filter(news -> news.getUpdatedAt() != null && news.getCreatedAt() != null)
                    .filter(news -> news.getUpdatedAt().toLocalDate().equals(targetDate))
                    .filter(news -> !news.getUpdatedAt().toLocalDate().equals(news.getCreatedAt().toLocalDate()) ||
                                  Duration.between(news.getCreatedAt(), news.getUpdatedAt()).toMinutes() >= 1)
                    .count();
                dailyEditsCount += newsEditedToday;
                
                // Count ChapChin news created on this day
                long chapChinCreatedToday = editorNews.stream()
                    .filter(news -> news.getCreatedAt() != null)
                    .filter(news -> news.getCreatedAt().toLocalDate().equals(targetDate))
                    .filter(news -> news.getChapChin() != null && news.getChapChin())
                    .count();
                dailyChapChinCount += chapChinCreatedToday;
            }
            
            weeklyNewsData.add(dailyNewsCount);
            weeklyEditsData.add(dailyEditsCount);
            weeklyChapChinData.add(dailyChapChinCount);
        }

        List<Map<String, Object>> editorContributions = new ArrayList<>();
        
        for (Users editor : allUsers) {
            // Skip admin users, only include editors
            if (editor.getUserTypeId().getUserTypeName().equals("Admin")) {
                continue;
            }
            
            List<News> editorNews = iNewsService.findByUserId(editor);
            List<UserMonthlyReport> editorReports = editor.getMonthlyReports();
            
            // Calculate total news created by this editor
            long totalNews = editorNews.size();
            
            // Calculate total edits by this editor
            long editorTotalEdits = editorReports.stream()
                .mapToLong(report -> report.getEditsCounter() != null ? report.getEditsCounter() : 0L)
                .sum();
            
            // Get editor's name
            String editorName = "نامشخص";
            if (editor.getUserProfile() != null) {
                String firstName = editor.getUserProfile().getFirstName();
                String lastName = editor.getUserProfile().getLastName();
                if (firstName != null && lastName != null) {
                    editorName = firstName + " " + lastName;
                } else if (firstName != null) {
                    editorName = firstName;
                } else if (lastName != null) {
                    editorName = lastName;
                } else {
                    // If no names are available, use email address
                    editorName = editor.getEmail() != null ? editor.getEmail() : "نامشخص";
                }
            } else {
                // If no profile exists, use email address
                editorName = editor.getEmail() != null ? editor.getEmail() : "نامشخص";
            }
            
            // Create editor contribution data
            Map<String, Object> editorData = new HashMap<>();
            editorData.put("name", editorName);
            editorData.put("articles", totalNews);
            editorData.put("edits", editorTotalEdits);
            
            editorContributions.add(editorData);
        }

        // Get recent news from all editors for the table (last 10 news)
        List<News> recentNews = new ArrayList<>();
        for (Users editor : allUsers) {
            if (editor.getUserTypeId().getUserTypeName().equals("Admin")) {
                continue;
            }
            List<News> editorNews = iNewsService.findByUserId(editor);
            recentNews.addAll(editorNews);
        }
        
        // Sort by creation date (most recent first) and limit to 10
        recentNews.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
        recentNews = recentNews.stream().limit(10).collect(Collectors.toList());
        
        // Convert to simplified format for JSON serialization
        List<Map<String, Object>> recentNewsSimple = recentNews.stream().map(news -> {
            Map<String, Object> newsData = new HashMap<>();
            newsData.put("title", news.getTitle());
            
            newsData.put("createdAt", news.getCreatedAt() != null ? news.getCreatedAt().toString() : "");
            
            newsData.put("yoastSeoPoint", news.getYoastSeoPoint());
            // newsData.put("sendToTinn", news.getSendToTinn() != null ? news.getSendToTinn() : false);
            
            // Get editor name
            Users newsEditor = news.getUserId();
            String editorName = "نامشخص";
            if (newsEditor != null) {
                if (newsEditor.getUserProfile() != null) {
                    String firstName = newsEditor.getUserProfile().getFirstName();
                    String lastName = newsEditor.getUserProfile().getLastName();
                    if (firstName != null && lastName != null) {
                        editorName = firstName + " " + lastName;
                    } else if (firstName != null) {
                        editorName = firstName;
                    } else if (lastName != null) {
                        editorName = lastName;
                    } else {
                        editorName = newsEditor.getEmail() != null ? newsEditor.getEmail() : "نامشخص";
                    }
                } else {
                    editorName = newsEditor.getEmail() != null ? newsEditor.getEmail() : "نامشخص";
                }
            }
            newsData.put("editorName", editorName);
            
            return newsData;
        }).collect(Collectors.toList());

        // Add attributes to model
        model.addAttribute("user", user);
        model.addAttribute("totalNewsAdded", totalNewsAdded);
        model.addAttribute("totalEdits", totalEdits);
        model.addAttribute("totalChapChinUsage", totalChapChinUsage);
        model.addAttribute("aiPercentage", Math.round(aiPercentage));
        model.addAttribute("weekLabels", weekLabels);
        model.addAttribute("weeklyNewsData", weeklyNewsData);
        model.addAttribute("weeklyEditsData", weeklyEditsData);
        model.addAttribute("weeklyChapChinData", weeklyChapChinData);
        model.addAttribute("editorContributions", editorContributions);
        model.addAttribute("recentNewsSimple", recentNewsSimple);
        model.addAttribute("pageTitle", "Admin Analytics");
        model.addAttribute("breadcrumbTitle", "آمار مدیریت");

        return "analytics/admin-analytics";
    }

    @GetMapping("/user/report")
    public String editorAnalytics(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = iUsersService.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("user not found."));

        // Get user's monthly reports (last 6 months)
        List<UserMonthlyReport> monthlyReports = user.getMonthlyReports();
        
        // Get all news by this user
        List<News> userNews = iNewsService.findByUserId(user);
        
        
        // AUTO: If user has news but no monthly reports, generate automaticly
        if (userNews != null && !userNews.isEmpty() && 
            (monthlyReports == null || monthlyReports.isEmpty())) {
            generateMonthlyReportsForExistingNews(user, userNews);
            // Refresh user data and monthly reports after generation 
            //REFRESH UPDATE
            user = iUsersService.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("user not found."));
            monthlyReports = user.getMonthlyReports();
        }
        
        // Calculate totals
        long totalNewsAdded = userNews.size();
        long totalAIAssisted = userNews.stream().mapToLong(news -> 
            (news.getChapChin() != null && news.getChapChin()) ? 1 : 0).sum();
        
        // Calculate total edits from monthly reports
        long totalEdits = monthlyReports != null ? monthlyReports.stream()
            .mapToLong(report -> report.getEditsCounter() != null ? report.getEditsCounter() : 0L)
            .sum() : 0;
        
        // Calculate AI assistance percentage
        double aiAssistancePercentage = totalNewsAdded > 0 ? 
            (double) totalAIAssisted / totalNewsAdded * 100 : 0;
        
        // Calculate real average SEO score from all user's news articles (include zeros)
        double avgSeoScore = userNews.stream()
            .mapToLong(News::getYoastSeoPoint)
            .average()
            .orElse(0.0);
                
        // Process monthly data for charts (last 6 months)
        YearMonth currentMonth = YearMonth.now();
        List<String> monthLabels = new ArrayList<>();
        List<Long> newsCountData = new ArrayList<>();
        List<Long> seoScoreData = new ArrayList<>();
        List<Long> editsCountData = new ArrayList<>();
        List<Long> aiUsageData = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            String monthLabel = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("fa-IR")) + " " + month.getYear();
            monthLabels.add(monthLabel);

            Optional<UserMonthlyReport> report = monthlyReports.stream()
                .filter(r -> r.getReportMonth().equals(month))
                .findFirst();

            if (report.isPresent()) {
                UserMonthlyReport monthlyReport = report.get();
                Long newsCount = monthlyReport.getNewsCounter() != null ? monthlyReport.getNewsCounter() : 0L;
                Long chapChinCount = monthlyReport.getChapChinCounter() != null ? monthlyReport.getChapChinCounter() : 0L;
                Long totalMonthlyNews = newsCount + chapChinCount;
                
                newsCountData.add(newsCount);
                seoScoreData.add(monthlyReport.getSeoPointAvg() != null ? monthlyReport.getSeoPointAvg() : 0L);
                editsCountData.add(monthlyReport.getEditsCounter() != null ? monthlyReport.getEditsCounter() : 0L);
                
                // Calculate AI usage percentage for this month
                Long aiUsagePercent = totalMonthlyNews > 0 ? (chapChinCount * 100 / totalMonthlyNews) : 0L;
                aiUsageData.add(aiUsagePercent);
            } else {
                newsCountData.add(0L);
                seoScoreData.add(0L);
                editsCountData.add(0L);
                aiUsageData.add(0L);
            }
        }
        
        // Get recent news (last 10) for the table
        List<News> recentNews = userNews.stream()
            .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
            .limit(10)
            .collect(Collectors.toList());
        
        
        // Prepare chart data
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("monthLabels", monthLabels);
        chartData.put("newsCountData", newsCountData);
        chartData.put("seoScoreData", seoScoreData);
        chartData.put("editsCountData", editsCountData);
        chartData.put("aiUsageData", aiUsageData);
        
        // Prepare simple data for template (avoid circular reference issues)
        List<Map<String, Object>> recentNewsSimple = recentNews.stream()
            .map(news -> {
                Map<String, Object> newsData = new HashMap<>();
                newsData.put("title", news.getTitle());
                newsData.put("createdAt", news.getCreatedAt().toString());
                newsData.put("yoastSeoPoint", news.getYoastSeoPoint());
                // newsData.put("sendToTinn", news.getSendToTinn() != null ? news.getSendToTinn() : false);
                return newsData;
            })
            .collect(Collectors.toList());
        
        // Get recent SEO scores for sparkline chart (last 10 news with SEO scores)
        List<Long> recentSeoScores = userNews.stream()
            .filter(news -> news.getYoastSeoPoint() > 0)
            .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
            .limit(10)
            .map(News::getYoastSeoPoint)
            .collect(Collectors.toList());
        
        // Prepare weekly data for the last 7 days (for weekly activity chart)
        LocalDate today = LocalDate.now();
        List<String> weekLabels = new ArrayList<>();
        List<Long> weeklyNewsData = new ArrayList<>();
        List<Long> weeklyEditsData = new ArrayList<>();
        List<Long> weeklyChapChinData = new ArrayList<>();
        List<Long> weeklySeoData = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String dayLabel = day.format(DateTimeFormatter.ofPattern("E", Locale.forLanguageTag("fa-IR")));
            weekLabels.add(dayLabel);
            
            // Count news created on this day
            long newsCountForDay = userNews.stream()
                .filter(news -> news.getCreatedAt().toLocalDate().equals(day))
                .count();
            weeklyNewsData.add(newsCountForDay);
            
            // Count edits for this day (count news updated on this day emroz mehr 9)
            long editsForDay = userNews.stream()
                .filter(news -> news.getUpdatedAt() != null)
                .filter(news -> news.getUpdatedAt().toLocalDate().equals(day))
                .count();
                
            
            weeklyEditsData.add(editsForDay);
            
            // Count ChapChin usage for this day, shamsi
            long chapChinCountForDay = userNews.stream()
                .filter(news -> news.getCreatedAt().toLocalDate().equals(day))
                .filter(news -> news.getChapChin() != null && news.getChapChin())
                .count();
            weeklyChapChinData.add(chapChinCountForDay);
            
            // Average SEO score for news created on this day (shamsi)
            OptionalDouble avgSeoForDay = userNews.stream()
                .filter(news -> news.getCreatedAt().toLocalDate().equals(day))
                .filter(news -> news.getYoastSeoPoint() > 0)
                .mapToLong(News::getYoastSeoPoint)
                .average();
            weeklySeoData.add(avgSeoForDay.isPresent() ? (long)avgSeoForDay.getAsDouble() : 0L);
        }

        // Add attributes to model
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("totalNewsAdded", totalNewsAdded);
        model.addAttribute("totalEdits", totalEdits);
        model.addAttribute("totalAIAssisted", totalAIAssisted);
        model.addAttribute("aiAssistancePercentage", Math.round(aiAssistancePercentage));
        model.addAttribute("avgSeoScore", Math.round(avgSeoScore));
        model.addAttribute("recentNewsSimple", recentNewsSimple);
        model.addAttribute("recentSeoScores", recentSeoScores);
        
        
        // Add weekly data to chart data
        chartData.put("weekLabels", weekLabels);
        chartData.put("weeklyNewsData", weeklyNewsData);
        chartData.put("weeklyEditsData", weeklyEditsData);
        chartData.put("weeklyChapChinData", weeklyChapChinData);
        chartData.put("weeklySeoData", weeklySeoData);
        
        model.addAttribute("chartData", chartData);
        model.addAttribute("pageTitle", "Editor Analytics");
        model.addAttribute("breadcrumbTitle", "آمار خبرنگار");

        return "analytics/editor-analytics";
    }
    
    /**
     * Generate monthly reports retroactively for existing news
     */
    private void generateMonthlyReportsForExistingNews(Users user, List<News> userNews) {
        try {
            Map<YearMonth, List<News>> newsByMonth = userNews.stream()
                .collect(Collectors.groupingBy(news -> 
                    YearMonth.from(news.getCreatedAt().toLocalDate())));
            
            for (Map.Entry<YearMonth, List<News>> entry : newsByMonth.entrySet()) {
                YearMonth month = entry.getKey();
                List<News> newsInMonth = entry.getValue();
                
                // Check if report already exists for this month
                Optional<UserMonthlyReport> existingReport = 
                    iUserMonthlyReportService.findByUserAndReportMonth(user, month);
                
                if (existingReport.isEmpty()) {
                    // Create new monthly report
                    UserMonthlyReport report = new UserMonthlyReport();
                    report.setUser(user);
                    report.setReportMonth(month);
                    
                    // Count regular news and ChapChin news
                    long regularNews = newsInMonth.stream()
                        .mapToLong(news -> (news.getChapChin() == null || !news.getChapChin()) ? 1 : 0)
                        .sum();
                    long chapChinNews = newsInMonth.stream()
                        .mapToLong(news -> (news.getChapChin() != null && news.getChapChin()) ? 1 : 0)
                        .sum();
                    
                    report.setNewsCounter(regularNews);
                    report.setChapChinCounter(chapChinNews);
                    report.setEditsCounter(0L); // Initialize edits counter
                    
                    // Calculate average SEO score
                    OptionalDouble avgOptional = newsInMonth.stream()
                        .mapToLong(News::getYoastSeoPoint)
                        .average();
                    
                    long avgSeoScore = avgOptional.isPresent() ? (long) avgOptional.getAsDouble() : 0L;
                    
                    report.setSeoPointAvg(avgSeoScore);
                    
                    // Save the report
                    iUserMonthlyReportService.save(report);
                    
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating monthly reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
