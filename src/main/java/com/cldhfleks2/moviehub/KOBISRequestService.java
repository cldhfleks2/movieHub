package com.cldhfleks2.moviehub;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
//KOBIS API로 요청을 전송, 응답받기를 담당
public class KOBISRequestService {
    //KOBSI API 요청을 줄이기위해 자주 쓰는 요청의 응답을 필드로 선언해서 저장
    private HttpResponse<String> totalTodayBoxOfficeResponse; 
    private HttpResponse<String> totalWeeklyBoxOfficeResponse;
    private HttpResponse<String> koreaWeeklyBoxOfficeResponse;
    private HttpResponse<String> foreignWeeklyBoxOfficeResponse;

    @Value("${kobis.key}")
    private String kobiskey;

    //KOBIS API URL로 요청을 보내고 응답을 리턴 해주는 함수 : 외부에서 사용할 수 도 있음
    public HttpResponse<String> sendRequest(String URL) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    //1. 전체일일박스오피스 10개 response Get
    public HttpResponse<String> getTotalTodayBoxOfficeResponse() {
        if (totalTodayBoxOfficeResponse == null) {
            throw new RuntimeException("API 응답이 없습니다. 서버와의 연결 문제일 수 있습니다.");
        } else if (totalTodayBoxOfficeResponse.statusCode() != 200) {
            throw new RuntimeException("API 호출이 실패했습니다. 응답 코드: " + totalTodayBoxOfficeResponse.statusCode());
        }
        return totalTodayBoxOfficeResponse;
    }

    //2. 전체주간박스오피스 10개 response Get
    public HttpResponse<String> getTotalWeeklyBoxOfficeResponse() {
        if (totalWeeklyBoxOfficeResponse == null) {
            throw new RuntimeException("API 응답이 없습니다. 서버와의 연결 문제일 수 있습니다.");
        } else if (totalWeeklyBoxOfficeResponse.statusCode() != 200) {
            throw new RuntimeException("API 호출이 실패했습니다. 응답 코드: " + totalWeeklyBoxOfficeResponse.statusCode());
        }
        return totalWeeklyBoxOfficeResponse;
    }

    //3. 주간한국박스오피스 10개 response Get
    public HttpResponse<String> getKoreaWeeklyBoxOfficeResponse() {
        if (koreaWeeklyBoxOfficeResponse == null) {
            throw new RuntimeException("API 응답이 없습니다. 서버와의 연결 문제일 수 있습니다.");
        } else if (koreaWeeklyBoxOfficeResponse.statusCode() != 200) {
            throw new RuntimeException("API 호출이 실패했습니다. 응답 코드: " + koreaWeeklyBoxOfficeResponse.statusCode());
        }
        return koreaWeeklyBoxOfficeResponse;
    }

    //4. 주간외국박스오피스 10개 response Get
    public HttpResponse<String> getForeignWeeklyBoxOfficeResponse() {
        if (foreignWeeklyBoxOfficeResponse == null) {
            throw new RuntimeException("API 응답이 없습니다. 서버와의 연결 문제일 수 있습니다.");
        } else if (foreignWeeklyBoxOfficeResponse.statusCode() != 200) {
            throw new RuntimeException("API 호출이 실패했습니다. 응답 코드: " + foreignWeeklyBoxOfficeResponse.statusCode());
        }
        return foreignWeeklyBoxOfficeResponse;
    }

    //1. KOBIS API 전체 일일 박스오피스 10개 : 응답을 필드에 저장
    public HttpResponse<String> sendTotalTodayBoxOfficeRequest() throws Exception {
        //KOBIS에서 통계가 하루 이전날 까지만 계산 되므로 날짜를 -1
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        String URL = "https://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json"
                + "?key=" + kobiskey
                + "&targetDt=" + currentDay;

        HttpResponse<String> response = sendRequest(URL);
        totalTodayBoxOfficeResponse = response; //필드에 저장
        return response;
    }

    //2. KOBIS API 전체주간박스오피스 10개 (월~일)(날짜를 포함하는 주간을 자동으로 인식해서 주나?)
    public HttpResponse<String> sendTotalWeeklyBoxOfficeRequest() throws Exception{
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        String URL = "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json"
                + "?key=" + kobiskey
                + "&targetDt=" + currentDay
                + "&weekGb=0";

        HttpResponse<String> response = sendRequest(URL);
        totalWeeklyBoxOfficeResponse = response; //필드에 저장
        return response;
    }

    //3. KOBIS API 주간한국박스오피스 10개 (월~일)
    public HttpResponse<String> sendKoreaWeeklyBoxOfficeRequest() throws Exception{
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        String URL = "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json"
                + "?key=" + kobiskey
                + "&targetDt=" + currentDay
                + "&weekGb=0"
                + "&repNationCd=K";

        HttpResponse<String> response = sendRequest(URL);
        koreaWeeklyBoxOfficeResponse = response; //필드에 저장
        return response;
    }

    //4. KOBIS API 주간외국박스오피스 10개 (월~일)
    public HttpResponse<String> sendForeignWeeklyBoxOfficeRequest() throws Exception{
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        String URL = "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json"
                + "?key=" + kobiskey
                + "&targetDt=" + currentDay
                + "&weekGb=0"
                + "&repNationCd=F";

        HttpResponse<String> response = sendRequest(URL);
        foreignWeeklyBoxOfficeResponse = response; //필드에 저장
        return response;
    }

    //영화 상세 정보 요청을 보내서 응답을 리턴해줌
    public HttpResponse<String> sendMovieDetailRequest(String movieCd) throws Exception{
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        String URL = "http://kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json"
                + "?key=" + kobiskey
                + "&movieCd=" + movieCd; //영화 코드를 보냄

        HttpResponse<String> response = sendRequest(URL);
        return response;
    }



}
