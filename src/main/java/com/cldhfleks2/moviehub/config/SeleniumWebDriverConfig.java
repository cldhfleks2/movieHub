package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.TMDBRequestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeleniumWebDriverConfig {
    private WebDriver driver;
    private boolean isDriverInitialized = false;

    @PostConstruct
    public void init() {
        initializeDriver();
    }

    @PreDestroy
    public void cleanup() {
        closeDriver();
    }

    private synchronized void initializeDriver() {
        if (!isDriverInitialized) {
            try {
                String driverPath = Paths.get("chromedriver", "chromedriver.exe").toAbsolutePath().toString();
                System.setProperty("webdriver.chrome.driver", driverPath);

                ChromeOptions options = new ChromeOptions();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--headless");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");

                driver = new ChromeDriver(options);
                isDriverInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeDriver() {
        if (isDriverInitialized && driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
                isDriverInitialized = false;
            }
        }
    }

    //웹크롤링으로 영화 포스터를 가져오는 코드
    public synchronized String getMoviePosterURL(String movieCd) {
        try {
            if (!isDriverInitialized) {
                initializeDriver();
            }

            String url = "https://www.moviechart.co.kr/info/movieinfo/trailer/" + movieCd;
            driver.get(url);

            // 알림창 처리
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                if (alert != null) {
                    String alertText = alert.getText();
                    alert.accept();
                    log.info("Alert handled for movie {}: {}", movieCd, alertText);
                    return null;
                }
            } catch (TimeoutException e) {
                // 알림창이 없는 경우 정상적으로 진행
            }

            WebElement posterElement = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.className("poster")));

            String posterUrl = posterElement.findElement(By.tagName("img")).getAttribute("src");
            return posterUrl;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error getting poster URL for movie {}", movieCd, e);
            // 에러 발생 시 드라이버 재초기화를 위해 닫기
            closeDriver();
            return null;
        }
    }

    // 드라이버 상태가 불안정할 때 수동으로 재시작하기 위한 메서드
    public synchronized void restartDriver() {
        closeDriver();
        initializeDriver();
    }
}
