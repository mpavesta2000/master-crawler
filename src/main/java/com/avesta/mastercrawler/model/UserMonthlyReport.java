package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;

@Entity
@Table(name = "user_monthly_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserMonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "report_month", nullable = false)
    private YearMonth reportMonth;

    @Column(name = "news_counter")
    private Long newsCounter;

    @Column(name = "seo_point_avg")
    private Long seoPointAvg;

    @Column(name = "chap_chin_counter")
    private Long chapChinCounter;
    
    @Column(name = "edits_counter")
    private Long editsCounter;
}
