package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.UserMonthlyReport;
import com.avesta.mastercrawler.model.Users;

import java.time.YearMonth;
import java.util.Optional;

public interface IUserMonthlyReportService {
    void updateUserReport(News news, long seoPoint);
    void updateUserReportForEdit(News news);
    Optional<UserMonthlyReport> findByUserAndReportMonth(Users user, YearMonth reportMonth);
    void save(UserMonthlyReport userMonthlyReport);
}
