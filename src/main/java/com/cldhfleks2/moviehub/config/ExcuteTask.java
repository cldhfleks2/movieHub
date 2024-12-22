package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.KOBISRequestService;
import com.cldhfleks2.moviehub.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcuteTask {
    private final MovieService movieService;
    private final KOBISRequestService kobisRequestService;

    @PostConstruct //앱 실행시 최초 1회 실행
    public void initialize()  {
        //자주 쓰이는 API요청을 미리 보내서 DB에 저장
        try{
            log.info("앱 실행 후 최초 작업 시작");
            //앱 실행시 최초 한번의 KOBIS API요청을 보내서 응답을 저장.
            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendTotalTodayBoxOfficeRequest();
            log.info("성공. 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendTotalWeeklyBoxOfficeRequest();
            log.info("성공. 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendKoreaWeeklyBoxOfficeRequest();
            log.info("성공. 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
            kobisRequestService.sendForeignWeeklyBoxOfficeRequest();
            log.info("성공. 응답을 정상적으로 가져옴.");

            //1.saveTodayBoxOfficeOnDB 사용
            log.info("전체 일일 박스오피스 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> totalTodayBoxOfficeResponse = kobisRequestService.getTotalTodayBoxOfficeResponse();
            movieService.saveTodayBoxOfficeOnDB(totalTodayBoxOfficeResponse);
            log.info("DB 저장 성공");
            //2.saveWeeklyBoxOfficeOnDB 사용
            log.info("전체 주간 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> totalWeeklyBoxOfficeResponse = kobisRequestService.getTotalWeeklyBoxOfficeResponse();
            movieService.saveWeeklyBoxOfficeOnDB(totalWeeklyBoxOfficeResponse);
            log.info("DB 저장 성공");
            //3.saveWeeklyBoxOfficeOnDB
            log.info("주간 한국 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> koreaWeeklyBoxOfficeResponse = kobisRequestService.getKoreaWeeklyBoxOfficeResponse();
            movieService.saveWeeklyBoxOfficeOnDB(koreaWeeklyBoxOfficeResponse);
            log.info("DB 저장 성공");
            //4.saveWeeklyBoxOfficeOnDB
            log.info("주간 외국 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
            HttpResponse<String> foreignWeeklyBoxOfficeResponse = kobisRequestService.getForeignWeeklyBoxOfficeResponse();
            movieService.saveWeeklyBoxOfficeOnDB(foreignWeeklyBoxOfficeResponse);
            log.info("DB 저장 성공");


        }catch (Exception e){
            e.printStackTrace();
            log.error("앱 실행후 초기화 작업중 에러 발생 => " +  e.getMessage());// 에러 출력
        }
    }


}
