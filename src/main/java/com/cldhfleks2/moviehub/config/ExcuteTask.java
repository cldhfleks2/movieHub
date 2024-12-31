package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.api.KOBISRequestService;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcuteTask {
    private final MovieService movieService;
    private final KOBISRequestService kobisRequestService;
    private final SeleniumWebDriver seleniumWebDriver;

    //앱 실행시 최초 1회 실행 되도록
    @PostConstruct
    //자주 쓰이는 API요청을 미리 보내서 DB에 저장
    public void initialize()  {
        try{
            log.info("System :: 자주 쓰는 API들 요청 시작");
            //1. API요청해서 response를 필드에 저장하는 함수
            sendCommonAPIrequest();

            //2. response를 이용해서 영화 정보를 DB에 저장하는 함수
            saveDBCommonAPIrequest();

            //3. DB에 저장된 값으로 메인함수에서 사용하는 movieDTOList를 미리 필드에 저장
            saveFieldCommonAPIrequest();
            log.info("System :: 자주 쓰는 API 요청 완료");

            log.info("System :: cgv 영화 순위 정보 가져오는중...");
            //4. 실시간 예매 순위 가져오기 : 웹 크롤링
            List<MovieDTO> movieRankList = seleniumWebDriver.getMovieRank();
            movieService.setMovieRankList(movieRankList);
            log.info("System :: cgv 영화 순위 정보 준비 완료");
        }catch (Exception e){
            e.printStackTrace();
            log.error("자주 쓰는 API 요청중 에러 발생 => " +  e.getMessage());// 에러 출력
        }
    }

    //1. API요청해서 response를 필드에 저장하는 함수
    private void sendCommonAPIrequest() throws Exception{
        HttpResponse response = null;
        log.info("전체 일일 박스오피스 목록 요청을 보내는중...");
        response = kobisRequestService.sendTotalTodayBoxOfficeRequest();
        if(response == null)
            log.error("전체 일일 박스오피스 요청 실패!!!!");
        else
            log.info("전체 일일 박스오피스 요청 성공.");

        log.info("전체 주간 박스오피스 목록 요청을 보내는중...");
        response = kobisRequestService.sendTotalWeeklyBoxOfficeRequest();
        if(response == null)
            log.error("전체 주간 박스오피스 요청 실패!!!!");
        else
            log.info("전체 주간 박스오피스 요청 성공.");

        log.info("주간 한국 박스오피스 목록 요청을 보내는중...");
        response = kobisRequestService.sendKoreaWeeklyBoxOfficeRequest();
        if(response == null)
            log.error("주간 한국 박스오피스 요청 실패!!!!");
        else
            log.info("주간 한국 박스오피스 요청 성공.");

        log.info("주간 외국 박스오피스 목록 요청을 보내는중...");
        response = kobisRequestService.sendForeignWeeklyBoxOfficeRequest();
        if(response == null)
            log.error("주간 외국 박스오피스 요청 실패!!!!");
        else
            log.info("주간 외국 박스오피스 요청 성공.");
    }

    //2. response를 이용해서 영화 정보를 DB에 저장하는 함수
    private void saveDBCommonAPIrequest() throws Exception{
        HttpResponse response = null;
        //1. movieService의 saveTodayBoxOfficeOnDB 사용
        log.info("전체 일일 박스오피스 목록의 응답을 DB에 저장 중... ...");
        HttpResponse<String> totalTodayBoxOfficeResponse = kobisRequestService.getTotalTodayBoxOfficeResponse();
        if(response == null)
            log.error("전체 일일 박스오피스 DB 저장 실패!!!!");
        else{
            movieService.saveTodayBoxOfficeOnDB(totalTodayBoxOfficeResponse);
            log.info("전체 일일 박스오피스 데이터를 DB 저장 성공");
        }

        //2. movieService의 saveWeeklyBoxOfficeOnDB 사용
        log.info("전체 주간 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
        HttpResponse<String> totalWeeklyBoxOfficeResponse = kobisRequestService.getTotalWeeklyBoxOfficeResponse();
        if(response == null)
            log.error("전체 주간 박스오피스 DB 저장 실패!!!!");
        else{
            movieService.saveWeeklyBoxOfficeOnDB(totalWeeklyBoxOfficeResponse);
            log.info("전체 주간 박스오피스 데이터를 DB 저장 성공");
        }

        //3. movieService의 saveWeeklyBoxOfficeOnDB
        log.info("주간 한국 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
        HttpResponse<String> koreaWeeklyBoxOfficeResponse = kobisRequestService.getKoreaWeeklyBoxOfficeResponse();
        if(response == null)
            log.error("주간 한국 박스오피스 DB 저장 실패!!!!");
        else{
            movieService.saveWeeklyBoxOfficeOnDB(koreaWeeklyBoxOfficeResponse);
            log.info("주간 한국 박스오피스 데이터를 DB 저장 성공");
        }

        //4. movieService의 saveWeeklyBoxOfficeOnDB
        log.info("주간 외국 박스오피스 10개 (월~일) 목록의 응답을 DB에 저장 중... ...");
        HttpResponse<String> foreignWeeklyBoxOfficeResponse = kobisRequestService.getForeignWeeklyBoxOfficeResponse();
        if(response == null)
            log.error("주간 외국 박스오피스 DB 저장 실패!!!!");
        else{
            movieService.saveWeeklyBoxOfficeOnDB(foreignWeeklyBoxOfficeResponse);
            log.info("주간 외국 박스오피스 데이터를 DB 저장 성공");
        }
    }

    //3. DB에 저장된 값으로 메인함수에서 사용하는 movieDTOList를 미리 필드에 저장
    private void saveFieldCommonAPIrequest() throws Exception{
        //1. 전체 일일 박스 오피스 API
        HttpResponse<String> totalTodayBoxOfficeResponse = kobisRequestService.getTotalTodayBoxOfficeResponse();
        List<MovieDTO> totalTodayBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(totalTodayBoxOfficeResponse, "dailyBoxOfficeList");
        movieService.setTotalTodayBoxOfficeMovie(totalTodayBoxOfficeMovie);

        //2. 전체 주간 박스오피스 API
        HttpResponse<String> totalWeeklyBoxOfficeResponse = kobisRequestService.getTotalWeeklyBoxOfficeResponse();
        List<MovieDTO> totalWeeklyBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(totalWeeklyBoxOfficeResponse, "weeklyBoxOfficeList");
        movieService.setTotalWeeklyBoxOfficeMovie(totalWeeklyBoxOfficeMovie);
        //3. 주간 한국 박스오피스 API
        HttpResponse<String> koreaWeeklyBoxOfficeResponse = kobisRequestService.getKoreaWeeklyBoxOfficeResponse();
        List<MovieDTO> koreaWeeklyBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(koreaWeeklyBoxOfficeResponse, "weeklyBoxOfficeList");
        movieService.setKoreaWeeklyBoxOfficeMovie(koreaWeeklyBoxOfficeMovie);
        //4. 주간 외국 박스오피스 API
        HttpResponse<String> foreignWeeklyBoxOffice = kobisRequestService.getForeignWeeklyBoxOfficeResponse();
        List<MovieDTO> foreignWeeklyBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(foreignWeeklyBoxOffice, "weeklyBoxOfficeList");
        movieService.setForeignWeeklyBoxOfficeMovie(foreignWeeklyBoxOfficeMovie);
    }

    // 매일 오전 6시 마다 API 요청을 미리 보냄
    @Scheduled(cron = "0 0 6 * * ?")
    public void executeDailyTask() {
        initialize();
    }



}
