package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcuteTask {
    private final MovieService movieService;

    @Value("${kobis.key}")
    private String kobiskey;



    @PostConstruct
    public void initialize()  {
        try{
            System.out.println("앱 실행 후 최초 작업 시작");
            //앱 실행시 최초 한번의 KOBIS API요청을 보내서 응답을 저장.
            movieService.sendTotalTodayBoxOfficeRequest();
            System.out.println("전체 일일 박스오피스 목록의 응답을 정상적으로 가져옴.");
            System.out.println("전체 일일 박스오피스 목록의 응답을 DB에 저장 시도...");
            movieService.saveTotalTodayBoxOffice();
            System.out.println("전체 일일 박스오피스 목록 DB 저장 성공");
            
            //그 외에 다른 API도

        }catch (Exception e){
            e.printStackTrace();
            log.info(e.getMessage());// 에러 출력
        }
    }




}
