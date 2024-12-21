package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
public class ExcuteTask {
    private HttpResponse<String> totalTodayBoxOfficeResponse;

    @Value("/kobis.key")
    private String kobiskey;

    // 응답을 다른 곳에서 활용하고 싶다면 이 메소드를 통해서 필드에 저장된 응답을 가져올 수 있음
    public HttpResponse<String> getTotalTodayBoxOfficeResponseBody() {
        if (totalTodayBoxOfficeResponse == null) {
            throw new RuntimeException("API 응답이 없습니다. 서버와의 연결 문제일 수 있습니다.");
        } else if (totalTodayBoxOfficeResponse.statusCode() != 200) {
            throw new RuntimeException("API 호출이 실패했습니다. 응답 코드: " + totalTodayBoxOfficeResponse.statusCode());
        }
        return totalTodayBoxOfficeResponse;
    }

    @PostConstruct
    public void initialize()  {
        try{
            System.out.println("앱 실행 후 초기화 작업 시작");
            //앱 실행시 최초 한번의 쿼리를 보냄.
            getTotalTodayBoxOfficeTask();


        }catch (Exception e){ //모든 에러를 캡쳐
            e.printStackTrace();
        }
    }

    //전체 일일 박스오피스 목록을 가져와서 필드로 지정
    public void getTotalTodayBoxOfficeTask() throws Exception {
        //KOBIS에서 통계가 하루 이전날 까지만 계산 되므로 날짜를 -1
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        //KOBIS API : 전체 일일박스오피스 10개 가져오는 요청 만들기
        String URL = "https://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json"
                + "?key=" + kobiskey
                + "&targetDt=" + currentDay;

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        totalTodayBoxOfficeResponse = response; //필드에 저장
    }


}
