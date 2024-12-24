package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.api.KOBISRequestService;
import com.cldhfleks2.moviehub.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcuteTask {
    private final MovieService movieService;
    private final KOBISRequestService kobisRequestService;

    //앱 실행시 최초 1회 실행 되도록
    @PostConstruct
    //자주 쓰이는 API요청을 미리 보내서 DB에 저장
    public void initialize()  {
        try{
            log.info("System :: 자주 쓰는 API들 요청 시작");
            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendTotalTodayBoxOfficeRequest();
            log.info("전체 일일 박스오피스 요청 성공.");

            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendTotalWeeklyBoxOfficeRequest();
            log.info("전체 일일 박스오피스 요청 성공.");

            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendKoreaWeeklyBoxOfficeRequest();
            log.info("전체 일일 박스오피스 요청 성공.");

            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendForeignWeeklyBoxOfficeRequest();
            log.info("전체 일일 박스오피스 요청 성공.");

            //1.saveTodayBoxOfficeOnDB 사용
            log.info("전체 일일 박스오피스 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> totalTodayBoxOfficeResponse = kobisRequestService.getTotalTodayBoxOfficeResponse();
            movieService.saveTodayBoxOfficeOnDB(totalTodayBoxOfficeResponse);
            log.info("전체 일일 박스오피스 데이터를 DB 저장 성공");
            //2.saveWeeklyBoxOfficeOnDB 사용
            log.info("전체 주간 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> totalWeeklyBoxOfficeResponse = kobisRequestService.getTotalWeeklyBoxOfficeResponse();
            movieService.saveWeeklyBoxOfficeOnDB(totalWeeklyBoxOfficeResponse);
            log.info("전체 주간 박스오피스 데이터를 DB 저장 성공");
            //3.saveWeeklyBoxOfficeOnDB
            log.info("주간 한국 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> koreaWeeklyBoxOfficeResponse = kobisRequestService.getKoreaWeeklyBoxOfficeResponse();
            movieService.saveWeeklyBoxOfficeOnDB(koreaWeeklyBoxOfficeResponse);
            log.info("주간 한국 박스오피스 데이터를 DB 저장 성공");
            //4.saveWeeklyBoxOfficeOnDB
            log.info("주간 외국 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> foreignWeeklyBoxOfficeResponse = kobisRequestService.getForeignWeeklyBoxOfficeResponse();
            movieService.saveWeeklyBoxOfficeOnDB(foreignWeeklyBoxOfficeResponse);
            log.info("주간 외국 박스오피스 데이터를 DB 저장 성공");
            log.info("System :: 자주 쓰는 API들 요청 완료");
        }catch (Exception e){
            e.printStackTrace();
            log.error("자주 쓰는 API 요청중 에러 발생 => " +  e.getMessage());// 에러 출력
        }
    }

    // 매일 오전 6시 마다 API 요청을 미리 보냄
    @Scheduled(cron = "0 0 6 * * ?")
    public void executeDailyTask() {
        initialize();
    }



}
