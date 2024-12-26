package com.cldhfleks2.moviehub.api;

import com.cldhfleks2.moviehub.config.SeleniumWebDriver;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
//TMDB API로 요청을 전송, 응답받기를 담당. 주로 포스터 이미지를 가져오는데 사용
public class TMDBRequestService {
    private final KOBISRequestService kobisRequestService;
    private final SeleniumWebDriver seleniumWebDriver;

    @Value("${tmdb.key}")
    private String tmdbkey;

    // URL 안전한 문자열로 인코딩 (UTF-8)
    public String encodeString(String keyword) throws Exception {
        return URLEncoder.encode(keyword, "UTF-8");
    }

    //TMDB API URL로 요청을 보내고 응답을 리턴 해주는 함수 : 외부에서 사용할 수 도 있음
    public HttpResponse<String> sendRequest(String URL) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //에러 발생시 null값 리턴
        if (response.statusCode() != 200 || (response.body() != null && response.body().startsWith("<"))) {
            log.error("getMovieDTOAsMovieList 에러발생 response.body(): {}", response.body());
            return null;
        }
        return response;
    }

    //TMDB API 영화이름 검색 : language지정
    public HttpResponse<String> sendSearchMovie(String movieNm, String language) throws Exception{
        // 영화 제목을 인코딩
        String encodedMovieNm = encodeString("{" + movieNm + "}");

        String URL = "https://api.themoviedb.org/3/search/movie"
                + "?api_key=" + tmdbkey
                + "&query=" + encodedMovieNm
                + "&language=" + language;

        //log.info("요청 URL >> " + URL);
        HttpResponse<String> response = sendRequest(URL);
        return response;
    }
    //TMDB API 영화이름 검색 : languae지정안하면 기본값 "ko"
    public HttpResponse<String> sendSearchMovie(String movieNm) throws Exception{
        return sendSearchMovie(movieNm, "ko"); //기본값
    }

    //TMDB API 인물이름 검색 : peopleNm으로 검색
    public HttpResponse<String> sendSearchPeople(String peopleNm, Long page) throws Exception{
        String encodedPersonNm = encodeString(peopleNm);

        String URL = "https://api.themoviedb.org/3/search/person"
                + "?api_key=" + tmdbkey
                + "&query=" + encodedPersonNm //영화 코드를 보냄
                + "&page=" + page;

//        log.info("요청 URL >> " + URL);

        HttpResponse<String> response = sendRequest(URL);
        return response;
    }

    //TMDB API 인물이 참여한 영화목록을 검색 : peopleId값
    //정렬기준을 최신순으로 저장
    public HttpResponse<String> sendSearchMovieListAsPeopleId(Long peopleId, Long page) throws Exception{
        //Long값이므로 인코딩 안함
        
        String URL = "https://api.themoviedb.org/3/discover/movie"
                + "?api_key=" + tmdbkey
                + "&with_cast=" + peopleId
                + "&sort_by=release_date.desc" //최신순으로
                + "&page=" + page;

        log.info("sendSearchMovieListAsPeopleId 요청 URL >> " + URL);
        HttpResponse<String> response = sendRequest(URL);
        return response;

    }


    //영화 포스터URL를 가져오는 함수
    //1차 : 셀레니움 웹 크롤링
    //2차 : TMDB에서 영화 한글 명으로 검색
    //3차 : TMDB에서 영화 영문 명으로 검색
    //response가 null을 가져옴에대한 예외 처리를 안했다.
    public String getMoviePostURL(String movieCd, String movieNm, String movieNmEn) throws Exception {
        log.info("getMoviePostURL 셀레니움으로 이미지 검색 시도... >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
        String posterURL =  seleniumWebDriver.getMoviePosterURL(movieCd);

        if(posterURL != null){ //못찾았을때 null값임
            log.info("getMoviePostURL 셀레니움으로 이미지 검색 성공 >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
            return posterURL;
        }else{ //2차 : TMDB에서 영화한글명 검색
            log.warn("getMoviePostURL 셀레니움 실패 >> movieCd: {}, movieCd:{}", movieCd, movieNm);
            log.info("getMoviePostURL TMDB에서 영화한글명 검색 시도... >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
            HttpResponse<String> response = sendSearchMovie(movieNm); //language지정 안하면 기본값 ko
            String responseBody = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultsNode = rootNode.path("results");
            //영화 검색 성공 : movieNm사용
            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode firstMovieNode = resultsNode.get(0);

                String posterPath = firstMovieNode.path("poster_path").asText();
                posterURL = "https://image.tmdb.org/t/p/w500" + posterPath;
                log.info("getMoviePostURL TMDB에서 영화한글명 검색 성공 >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
                return posterURL;
            } else { //3차 : TMDB에서 영화영문명 검색
                log.warn("getMoviePostURL TMDB에서 영화한글명 검색 실패 >> movieCd: {}, movieCd:{}", movieCd, movieNm);
                log.info("getMoviePostURL TMDB에서 영화영문명 검색 시도... >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
                response = sendSearchMovie(movieNmEn, "en");
                responseBody = response.body();
                //log.info("TMDB Response >> " + responseBody);
                objectMapper = new ObjectMapper();
                rootNode = objectMapper.readTree(responseBody);
                resultsNode = rootNode.path("results");

                //똑같은 분기 : 영화 검색 성공 (movieNmEn사용)
                if (resultsNode.isArray() && resultsNode.size() > 0) {
                    JsonNode firstMovieNode = resultsNode.get(0);

                    String posterPath = firstMovieNode.path("poster_path").asText();
                    posterURL = "https://image.tmdb.org/t/p/w500" + posterPath;
                    log.info("getMoviePostURL TMDB에서 영화영문명 검색 성공 >>  movieCd: {}, movieNm:{}", movieCd, movieNm);
                    return posterURL;
                }else{
                    log.error("getMoviePostURL 포스터를 가져오는데 3차 전부 실패 >>  movieCd: {}, movieNm:{}", movieCd, movieNm);
                    return null; //TMDB에서 movieNmEn으로 검색 실패
                }

            }

        }
    }

    //영화 포스터URL를 빠르게 가져오자. 셀레니움없이
    //1차 : TMDB에서 영화 한글 명으로 검색
    //2차 : TMDB에서 영화 영문 명으로 검색
    public String getMoviePostURLFAST(String movieCd, String movieNm, String movieNmEn) throws Exception {
        log.info("getMoviePostURLFAST TMDB에서 영화한글명 검색 시도... >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
        HttpResponse<String> response = sendSearchMovie(movieNm); //language지정 안하면 기본값 ko
        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("results");
        //영화 검색 성공 : movieNm사용
        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstMovieNode = resultsNode.get(0);

            String posterPath = firstMovieNode.path("poster_path").asText();
            String posterURL = "https://image.tmdb.org/t/p/w500" + posterPath;
            log.info("getMoviePostURLFAST TMDB에서 영화한글명 검색 성공 >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
            return posterURL;
        } else { //3차 : TMDB에서 영화영문명 검색
            log.warn("getMoviePostURLFAST TMDB에서 영화한글명 검색 실패 >> movieCd: {}, movieCd:{}", movieCd, movieNm);
            log.info("getMoviePostURLFAST TMDB에서 영화영문명 검색 시도... >>  movieCd: {}, movieCd:{}", movieCd, movieNm);
            response = sendSearchMovie(movieNmEn, "en");
            responseBody = response.body();
            //log.info("TMDB Response >> " + responseBody);
            objectMapper = new ObjectMapper();
            rootNode = objectMapper.readTree(responseBody);
            resultsNode = rootNode.path("results");

            //똑같은 분기 : 영화 검색 성공 (movieNmEn사용)
            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode firstMovieNode = resultsNode.get(0);

                String posterPath = firstMovieNode.path("poster_path").asText();
                String posterURL = "https://image.tmdb.org/t/p/w500" + posterPath;
                log.info("getMoviePostURLFAST TMDB에서 영화영문명 검색 성공 >>  movieCd: {}, movieNm:{}", movieCd, movieNm);
                return posterURL;
            }else{
                log.error("getMoviePostURLFAST 포스터를 가져오는데 전부 실패 >>  movieCd: {}, movieNm:{}", movieCd, movieNm);
                return null; //TMDB에서 movieNmEn으로 검색 실패
            }
        }
    }


}
