package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.config.ExcuteTask;
import com.cldhfleks2.moviehub.config.SeleniumWebDriverConfig;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.movie.MovieService;
import com.cldhfleks2.moviehub.movie.audit.MovieAudit;
import com.cldhfleks2.moviehub.movie.audit.MovieAuditRepository;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStatRepository;
import com.cldhfleks2.moviehub.movie.genre.MovieGenre;
import com.cldhfleks2.moviehub.movie.genre.MovieGenreRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasicService {
    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final SeleniumWebDriverConfig seleniumWebDriverConfig;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieAuditRepository movieAuditRepository;
    private final MovieDailyStatRepository movieDailyStatRepository;

    @Value("${kobis.key}")
    private String kobiskey;

    String test(Model model) throws Exception {
        HttpResponse<String> totalTodayBoxOfficeResponse = movieService.getTotalTodayBoxOfficeResponse();
        String totalTodayBoxOfficeResponseBody = totalTodayBoxOfficeResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(totalTodayBoxOfficeResponseBody);
        //테스트
        movieService.translateJsonToMap(jsonNode);
        return "test";
    }

    //메인 페이지 GET
    String getMain(Model model) throws Exception{
        //전체 일일 박스 오피스를 담은 response를 JSON파싱 진행
        HttpResponse<String> totalTodayBoxOfficeResponse = movieService.getTotalTodayBoxOfficeResponse();
        String totalTodayBoxOfficeResponseBody = totalTodayBoxOfficeResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(totalTodayBoxOfficeResponseBody);
        JsonNode totalTodayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");

        //response에서 영화 목록만을 참고하려함. 실제 영화 데이터는 이미 DB에 존재할것.
        List<MovieDTO> totalTodayBoxOfficeMovie = new ArrayList<>();
        for (int i = 0; i < totalTodayBoxOfficeList.size() && i < 10; i++) {
            //영화 목록중 현재 영화에 대한 처리
            JsonNode movieJsonNode = totalTodayBoxOfficeList.get(i);
            String movieCd = movieJsonNode.path("movieCd").asText();
            Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
            //1. 등록되지 않은 영화는 제외
            if(!movieObj.isPresent()) continue;
            //2. 미공개 영화는 제외
            if(movieObj.get().getStatus() == 0) continue;
            Movie movie = movieObj.get();
            Long movieId = movie.getId();
            //3. 사용할 필드만 DTO에 담는다.
            MovieDTO.MovieDTOBuilder movieDTOBuilder = MovieDTO.builder();
            movieDTOBuilder.movieCd(movie.getMovieCd());
            movieDTOBuilder.movieNm(movie.getMovieNm());
            movieDTOBuilder.showTm(movie.getShowTm());
            movieDTOBuilder.openDt(movie.getOpenDt());
            movieDTOBuilder.audiAcc(movie.getAudiAcc());
            movieDTOBuilder.posterURL(movie.getPosterURL());
            //3-1. 장르 가져오기 (MovieGenre에서)
            List<MovieGenre> movieGenreList = movieGenreRepository.findByMovieIdAndStatus(movieId);
            if(movieGenreList.isEmpty()){
                System.out.println("다음에 출력할 영화 객체의 MovieGenre객체에 문제가 있음 = " + movie);
                continue;
            }
            movieDTOBuilder.genreList(movieGenreList);
            //3-2. 시청 가이드라인 가져오기 (MovieAudit에서)
            List<MovieAudit> movieAuditList = movieAuditRepository.findByMovieIdAndStatus(movieId);
            if(movieAuditList.isEmpty()){
                System.out.println("다음에 출력할 영화 객체의 MovieAudit객체에 문제가 있음 = " + movie);
                continue;
            }
            movieDTOBuilder.watchGradeList(movieAuditList);
            //3-3. 관객수 가져오기 (MovieDailyStat에서)
            List<MovieDailyStat> movieDailyStatList = movieDailyStatRepository.findByMovieIdAndStatus(movieId);
            if(movieDailyStatList.isEmpty()){
                System.out.println("다음에 출력할 영화 객체의 MovieDailySta객체에 문제가 있음 = " + movie);
                continue;
            }
            movieDTOBuilder.audiCnt(movieDailyStatList.get(0).getAudiCnt()); //값만 보냄

            //4. 마지막으로 결과를 담음
            MovieDTO movieDTO = movieDTOBuilder.build();
            totalTodayBoxOfficeMovie.add(movieDTO);
        }
        //5. 모델에 데이터 전달
        model.addAttribute("totalTodayBoxOfficeMovie", totalTodayBoxOfficeMovie);
        
        return "main/main";
    }


    String getDetail(String movieCd, Model model) {
        //movieCd로 영화를 찾고 없으면
        //KOBIS API로 영화를 검색해봄.
        //그래도 없으면 메인페이지로 리다이렉트 alertmessage보내면서

        return "detail/detail";
    }

    //JS에서 KOBIS Key를 요청하면 String으로 전달해준다.
    ResponseEntity<String> getKobiskey() {
        return ResponseEntity.ok(kobiskey);
    }



}
