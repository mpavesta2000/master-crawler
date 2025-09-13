package com.avesta.mastercrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class MasterCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MasterCrawlerApplication.class, args);
    }

}
