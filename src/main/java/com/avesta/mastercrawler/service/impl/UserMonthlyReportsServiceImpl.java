package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.UserMonthlyReport;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.repository.NewsRepository;
import com.avesta.mastercrawler.repository.UserMonthlyReportRepository;
import com.avesta.mastercrawler.service.IUserMonthlyReportService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserMonthlyReportsServiceImpl implements IUserMonthlyReportService {

    private final UserMonthlyReportRepository userMonthlyReportRepository;
    private final NewsRepository newsRepository;

    @Override
    public void updateUserReport(News news, long seoPoint) {
        Users user = news.getUserId();
        YearMonth newsMonth = YearMonth.from(news.getCreatedAt().toLocalDate());

                UserMonthlyReport report = userMonthlyReportRepository.findByUserAndReportMonth(user, newsMonth)
                .orElseGet(() -> {
                    UserMonthlyReport newReport = new UserMonthlyReport();
                    newReport.setUser(user);
                    newReport.setReportMonth(newsMonth);
                    newReport.setNewsCounter(0L);
                    newReport.setChapChinCounter(0L);
                    newReport.setSeoPointAvg(0L);
                    newReport.setEditsCounter(0L);
                    return newReport;
                });

        if(!news.getChapChin()) {
            Long currentCount = report.getNewsCounter();
            report.setNewsCounter((currentCount != null ? currentCount : 0L) + 1);
        }else {
            Long currentCount = report.getChapChinCounter();
            report.setChapChinCounter((currentCount != null ? currentCount : 0L) + 1);
        }

        Long newsCount = report.getNewsCounter() != null ? report.getNewsCounter() : 0L;
        Long chapChinCount = report.getChapChinCounter() != null ? report.getChapChinCounter() : 0L;
        long totalNewsCount = newsCount + chapChinCount;

        if (totalNewsCount > 0) {
            double averageSeoPoints = user.getNews()
                    .stream()
                    .mapToLong(News::getYoastSeoPoint)
                    .average()
                    .orElse(0);
            report.setSeoPointAvg((long) averageSeoPoints);
        }
        userMonthlyReportRepository.save(report);
    }

    @Override
    public void updateUserReportForEdit(News news) {
        Users user = news.getUserId();
        // Use the news creation date to find the correct monthly report
        YearMonth newsMonth = YearMonth.from(news.getCreatedAt().toLocalDate());

        UserMonthlyReport report = userMonthlyReportRepository
                .findByUserAndReportMonth(user, newsMonth)
                .orElseGet(() -> {
                    // Create report if it doesn't exist (for existing news)
                    UserMonthlyReport newReport = new UserMonthlyReport();
                    newReport.setUser(user);
                    newReport.setReportMonth(newsMonth);
                    newReport.setNewsCounter(0L);
                    newReport.setChapChinCounter(0L);
                    newReport.setSeoPointAvg(0L);
                    newReport.setEditsCounter(0L);
                    return newReport;
                });

        // Increment the edits counter
        Long currentEdits = report.getEditsCounter() != null ? report.getEditsCounter() : 0L;
        report.setEditsCounter(currentEdits + 1);

        // Recalculate the average SEO score for this report
        recalculateReportFromDatabase(user, newsMonth);
        
        // Save the updated report
        userMonthlyReportRepository.save(report);
    }

    private void recalculateReportFromDatabase(Users user, YearMonth month) {
        UserMonthlyReport report = userMonthlyReportRepository
                .findByUserAndReportMonth(user, month)
                .orElse(null);

        if (report != null) {
            // Get start and end dates for the month when the news was created
            LocalDate startDate = month.atDay(1);
            LocalDate endDate = month.atEndOfMonth();

            // Fetch all news for this user created in this specific month
            List<News> newsInMonth = newsRepository.findByUserIdAndCreatedAtBetween(
                    user,
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );

            // Recalculate the average SEO point for this month's report
            if (!newsInMonth.isEmpty()) {
                long totalSeoPoints = newsInMonth.stream()
                        .mapToLong(news -> Optional.ofNullable(news.getYoastSeoPoint()).orElse(0L))
                        .sum();

                long newAvg = totalSeoPoints / newsInMonth.size();

                // Update the report with recalculated values
                report.setSeoPointAvg(newAvg);

                userMonthlyReportRepository.save(report);
            }
        }
    }

    @Override
    public Optional<UserMonthlyReport> findByUserAndReportMonth(Users user, YearMonth reportMonth) {
        return userMonthlyReportRepository.findByUserAndReportMonth(user,reportMonth);
    }

    @Override
    public void save(UserMonthlyReport userMonthlyReport) {
        userMonthlyReportRepository.save(userMonthlyReport);
    }
}
