package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.config.SeleniumWebDriverConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
@Slf4j
//TMDB API로 요청을 전송, 응답받기를 담당. 주로 포스터 이미지를 가져오는데 사용
public class TMDBRequestService {
    private final KOBISRequestService kobisRequestService;
    private final SeleniumWebDriverConfig seleniumWebDriverConfig;

    @Value("${tmdb.key}")
    private String tmdbkey;

    //영화 제목을 URL-safe로 인코딩
    //{ } 사이로 보내는게 특징
    public String encodeMovieTitle(String movieNm) throws Exception {
        // URL 안전한 문자열로 인코딩 (UTF-8)
        String URL = "{" + movieNm + "}";
        return URLEncoder.encode(URL, "UTF-8");
    }

    //TMDB API URL로 요청을 보내고 응답을 리턴 해주는 함수 : 외부에서 사용할 수 도 있음
    public HttpResponse<String> sendRequest(String URL) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    //TMDB API 영화이름 검색 : languae지정안하면 기본값 "ko"
    public HttpResponse<String> sendSearchMovie(String movieNm) throws Exception{
        return sendSearchMovie(movieNm, "ko"); //기본값
    }
    //TMDB API 영화이름 검색 : language지정
    public HttpResponse<String> sendSearchMovie(String movieNm, String language) throws Exception{
        // 영화 제목을 인코딩
        String encodedMovieNm = encodeMovieTitle(movieNm);

        String URL = "https://api.themoviedb.org/3/search/movie"
                + "?api_key=" + tmdbkey
                + "&query=" + encodedMovieNm
                + "&language=" + language;

//        log.info("요청 URL >> " + URL);
        HttpResponse<String> response = sendRequest(URL);
        return response;
    }

    //3차 시도 웹 크롤링 (셀레니움)
    public String getMoviePostURLBySelenium(String movieNm, String movieNmEn, String movieCd){
        String posterURL =  seleniumWebDriverConfig.getMoviePosterURL(movieCd);
        if(!posterURL.isEmpty()){
            return posterURL;
        }else{ //그래도 못찾으면 gg
            log.error("'{}, {}' 포스터를 가져오는데 오류 발생 >> ", movieNm, movieNmEn);
            return null;
        }
    }

    //2차 시도 (movieNmEn)
    public String getMoviePosterURLByEn(String movieNm, String movieNmEn, String movieCd) throws Exception {
        //영문이름으로 검색해서 결과를 가져옴
        HttpResponse<String> response = sendSearchMovie(movieNmEn, "en");
        String responseBody = response.body();
        //log.info("TMDB Response >> " + responseBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");

        //똑같은 분기 : 영화 검색 성공 (movieNmEn사용)
        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstMovieNode = resultsNode.get(0); //문제가 되진 않겠지? 같은 변수명을 두번 선언해서..

            String posterPath = firstMovieNode.path("poster_path").asText();
            String posterURL = "https://image.tmdb.org/t/p/w500" + posterPath;
            return posterURL;
        }else{ //movieNmEn으로도 TMDB에서 결과를 못가져오면 3차 시도
            return getMoviePostURLBySelenium(movieNm, movieNmEn, movieCd);
        }
    }

    //영화 포스터URL를 가져오는 함수 1차로 시도(movieNm)
    public String getMoviePosterURL(String movieNm) throws Exception {
        HttpResponse<String> response = sendSearchMovie(movieNm);
        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");
        //영화 검색 성공 : movieNm사용
        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstMovieNode = resultsNode.get(0);

            String posterPath = firstMovieNode.path("poster_path").asText();
            String posterURL = "https://image.tmdb.org/t/p/w500" + posterPath;
            return posterURL;
        } else { //영화 검색 실패 : movieNm사용 
            //영문이름을 찾기위해 KOBIS API로 영화목록을 가져옴
            response = kobisRequestService.sendMovieListRequestByMovieNm(movieNm);
            responseBody = response.body();
            objectMapper = new ObjectMapper();
            rootNode = objectMapper.readTree(responseBody);
            //영화목록에서 movieNmEn영문이름을 찾음
            JsonNode movieListNode = rootNode.path("movieListResult").path("movieList");
            JsonNode movieNmEnNode = movieListNode.isArray() && movieListNode.size() > 0
                    ? movieListNode.get(0).path("movieNmEn")
                    : null;
            String movieNmEn = movieNmEnNode.asText();

            JsonNode movieCdNode = movieListNode.isArray() && movieListNode.size() > 0
                    ? movieListNode.get(0).path("movieCd")
                    : null;
            String movieCd = movieCdNode.asText();

            return getMoviePosterURLByEn(movieNm, movieNmEn, movieCd);
        }
    }
}
