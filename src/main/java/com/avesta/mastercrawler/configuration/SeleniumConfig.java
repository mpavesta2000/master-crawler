package com.avesta.mastercrawler.configuration;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

@Configuration
public class SeleniumConfig {

    @Value("${selenium.chrome.headless:true}")
    private boolean headless;

    @Value("${selenium.chrome.timeout:30}")
    private int timeout;

    @PostConstruct
    public void init() {
        WebDriverManager.chromedriver().setup();
    }

    @Bean
    @Scope("prototype")
    public WebDriver webDriver() {
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        return driver;
    }
}
