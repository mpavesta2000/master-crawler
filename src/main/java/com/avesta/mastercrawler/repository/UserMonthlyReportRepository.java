package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.UserMonthlyReport;
import com.avesta.mastercrawler.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface UserMonthlyReportRepository extends JpaRepository<UserMonthlyReport, Integer> {
    Optional<UserMonthlyReport> findByUserAndReportMonth(Users user, YearMonth reportMonth);
}
