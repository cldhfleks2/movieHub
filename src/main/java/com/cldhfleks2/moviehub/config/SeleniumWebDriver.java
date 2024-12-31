package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.movie.MovieDTO;
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
import java.util.ArrayList;
import java.util.List;

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

    public List<MovieDTO> getMovieRank() throws Exception{
        WebDriver driver = null;
        List<MovieDTO> movieDTOList = new ArrayList<>();

        // ChromeDriver 경로 설정
        String driverPath = Paths.get("chromedriver", "chromedriver.exe").toAbsolutePath().toString();
        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");

        driver = new ChromeDriver(options);
        driver.get("https://www.moviechart.co.kr/rank/realtime/index/image");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("movieBox-list")));

        // 영화 순위 정보가 포함된 요소들 찾기
        List<WebElement> movieItems = driver.findElements(By.cssSelector(".movieBox-item"));
        for (WebElement movieItem : movieItems) {
            // movieCd 추출
            String movieCd = movieItem.findElement(By.tagName("a"))
                    .getAttribute("href")
                    .replaceAll(".*/detail/(\\d+).*", "$1");

            // rank 추출
            Long rank = Long.parseLong(
                    movieItem.findElement(By.className("rank"))
                            .getText()
                            .trim()
            );

            // movieNm 추출
            String movieNm = movieItem.findElement(By.cssSelector(".movie-title h3 a"))
                    .getText()
                    .trim();

            // age 추출
            WebElement ageElement = movieItem.findElement(By.className("movie-age"));
            String age = ageElement.getAttribute("class")
                    .replaceAll(".*(age\\d*).*", "$1");

            // posterUrl 추출
            String imgTagSrcURL = movieItem.findElement(By.tagName("img"))
                    .getAttribute("src");
            String posterURL = imgTagSrcURL.split("source=")[1]; //실제 이미지 주소만 가져옴

            // releaseDate 추출
            String releaseDate = movieItem.findElement(By.cssSelector(".movie-txt .movie-launch"))
                    .getText()
                    .replace("개봉일 : ", "")
                    .trim();

            // ticketRate 추출
            String ticketRate = movieItem.findElement(By.cssSelector(".movie-txt .ticketing span"))
                    .getText()
                    .trim();

            // 데이터 저장
            MovieDTO movieDTO = MovieDTO.create()
                    .movieCd(movieCd)
                    .rank(rank)
                    .movieNm(movieNm)
                    .age(age)
                    .posterURL(posterURL)
                    .releaseDate(releaseDate)
                    .ticketRate(ticketRate)
                    .build();
            movieDTOList.add(movieDTO);
        }

        if (driver != null) {
            driver.quit();
        }

        return movieDTOList;
    }


}
