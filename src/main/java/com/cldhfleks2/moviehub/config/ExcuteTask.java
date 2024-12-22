package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.KOBISRequestService;
import com.cldhfleks2.moviehub.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
            log.info("전체 일일 박스오피스 목록요청을 보내는중...");
            kobisRequestService.sendTotalTodayBoxOfficeRequest();
            log.info("전체 일일 박스오피스 목록의 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록요청을 보내는중...");
            kobisRequestService.sendTotalWeeklyBoxOfficeRequest();
            log.info("전체 일일 박스오피스 목록의 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록요청을 보내는중...");
            kobisRequestService.sendKoreaWeeklyBoxOfficeRequest();
            log.info("전체 일일 박스오피스 목록의 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록요청을 보내는중...");
            kobisRequestService.sendForeignWeeklyBoxOfficeRequest();
            log.info("전체 일일 박스오피스 목록의 응답을 정상적으로 가져옴.");

            log.info("전체 일일 박스오피스 목록의 응답을 DB에 저장 중... ...");
            movieService.saveTodayBoxOffice();
            log.info("전체 일일 박스오피스 목록 DB 저장 성공");
            //그 외에 다른 API도

        }catch (Exception e){
            e.printStackTrace();
            log.info(e.getMessage());// 에러 출력
        }
    }


}
