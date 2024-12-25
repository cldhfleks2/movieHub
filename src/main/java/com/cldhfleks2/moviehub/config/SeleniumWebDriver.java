package com.cldhfleks2.moviehub.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeleniumWebDriver {

    public String getMoviePosterURL(String movieCd) {
        WebDriver localDriver = null;
        try {
            // 크롬 드라이버 경로 설정
            String driverPath = Paths.get("chromedriver", "chromedriver.exe").toAbsolutePath().toString();
            System.setProperty("webdriver.chrome.driver", driverPath);

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--incognito"); // 비공개 모드로 실행
            options.addArguments("--disable-cache"); // 캐시 비활성화

            localDriver = new ChromeDriver(options);

            // URL 요청
            String url = "https://www.moviechart.co.kr/info/movieinfo/trailer/" + movieCd;
            localDriver.get(url);
            log.info("Requested movieCd: {}", movieCd);
            log.info("Requested URL: {}", url);

            // 알림창 처리
            try {
                WebDriverWait wait = new WebDriverWait(localDriver, Duration.ofSeconds(10));
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                if (alert != null) {
                    String alertText = alert.getText();
                    log.info("Alert handled for movie {}: {}", movieCd, alertText);
                    alert.accept();
                    return null;
                }
            } catch (TimeoutException e) {
                // 알림창이 없는 경우 정상적으로 진행
            }

            WebElement posterElement = new WebDriverWait(localDriver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.className("poster")));

            String posterUrl = posterElement.findElement(By.tagName("img")).getAttribute("src");
            log.info("posterUrl: {}", posterUrl);
            return posterUrl;

        } catch (Exception e) {
            log.error("Error getting poster URL for movie {}", movieCd, e);
            return null;
        } finally {
            if (localDriver != null) {
                localDriver.quit();
            }
        }
    }

//    public String getMoviePosterURL(String movieCd) {
//        final String[] result = new String[1];
//        Thread thread = new Thread(() -> {
//            WebDriver driver = null;
//            try {
//                String driverPath = Paths.get("chromedriver", "chromedriver.exe").toAbsolutePath().toString();
//                System.setProperty("webdriver.chrome.driver", driverPath);
//
//                ChromeOptions options = new ChromeOptions();
//                options.addArguments("--remote-allow-origins=*");
//                options.addArguments("--headless");
//                options.addArguments("--disable-gpu");
//                options.addArguments("--no-sandbox");
//                options.addArguments("--incognito");
//                options.addArguments("--disable-cache");
//
//                driver = new ChromeDriver(options);
//
//
//                String url = "https://www.moviechart.co.kr/info/movieinfo/trailer/" + movieCd;
//                driver.get(url);
//                log.info("Thread {} - Processing movieCd: {}", Thread.currentThread().getId(), movieCd);
//                log.info("Thread {} - Accessing URL: {}", Thread.currentThread().getId(), url);
//
//                // Alert 처리
//                try {
//                    Alert alert = new WebDriverWait(driver, Duration.ofSeconds(3))
//                            .until(ExpectedConditions.alertIsPresent());
//                    if (alert != null) {
//                        String alertText = alert.getText();
//                        log.info("Thread {} - Alert for movieCd {}: {}",
//                                Thread.currentThread().getId(), movieCd, alertText);
//                        alert.accept();
//                        result[0] = null;
//                        return;
//                    }
//                } catch (TimeoutException ignored) {
//                    // 알림창이 없는 경우 계속 진행
//                }
//
//                WebElement posterElement = new WebDriverWait(driver, Duration.ofSeconds(10))
//                        .until(ExpectedConditions.presenceOfElementLocated(By.className("poster")));
//
//                result[0] = posterElement.findElement(By.tagName("img")).getAttribute("src");
//                log.info("Thread {} - Found poster URL for movieCd {}: {}",
//                        Thread.currentThread().getId(), movieCd, result[0]);
//
//            } catch (Exception e) {
//                log.error("Thread {} - Error processing movieCd {}",
//                        Thread.currentThread().getId(), movieCd, e);
//                result[0] = null;
//            } finally {
//                if (driver != null) {
//                    try {
//                        driver.quit();
//                    } catch (Exception e) {
//                        log.error("Error closing driver", e);
//                    }
//                }
//            }
//        });
//
//        thread.start();
//        try {
//            thread.join(15000); // 15초 타임아웃
//            if (thread.isAlive()) {
//                thread.interrupt();
//                log.error("Timeout while processing movieCd: {}", movieCd);
//                return null;
//            }
//        } catch (InterruptedException e) {
//            log.error("Thread interrupted while processing movieCd: {}", movieCd);
//            thread.interrupt();
//            return null;
//        }
//
//        return result[0];
//    }


}
