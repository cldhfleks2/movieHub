package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.config.ExcuteTask;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.movie.MovieService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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
    private final ExcuteTask excuteTask;
    private final MovieRepository movieRepository;

    @Value("${kobis.key}")
    private String kobiskey;

    //메인 페이지 GET
    String getMain(Model model) throws Exception{
        //전체 일일 박스 오피스 목록을 DB에 저장 (이미 존재하면 저장 안하도록)
        movieService.saveTodayBoxOffice();
        //전체 일일 박스 오피스 10개를 가져와서 모델에 담는다.
        HttpResponse<String> totalTodayBoxOfficeResponse = excuteTask.getTotalTodayBoxOfficeResponseBody();
        String totalTodayBoxOfficeResponseBody = totalTodayBoxOfficeResponse.body();
        //JSON파싱 진행
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(totalTodayBoxOfficeResponseBody);
        JsonNode totalTodayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");
        
        List<MovieDTO> totalTodayBoxOfficeMovie = new ArrayList<>();
        //응답의 영화 목록에 대해 반복
        for (int i = 0; i < totalTodayBoxOfficeList.size() && i < 10; i++) {
            //현재 영화에 대한 처리
            JsonNode movieJsonNode = totalTodayBoxOfficeList.get(i);
            String movieCd = movieJsonNode.path("movieCd").asText();
            Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
            if(!movieObj.isPresent()) continue;
            //필요한것만 넣는다.
            MovieDTO.MovieDTOBuilder movieDTOBuilder = MovieDTO.builder();
            movieDTOBuilder.movieCd(movieJsonNode.path("movieCd").asText());
            movieDTOBuilder.movieNm(movieJsonNode.path("movieNm").asText());
            movieDTOBuilder.watchGradeNm(movieJsonNode.path("watchGradeNm").asText());
            movieDTOBuilder.genreNm(movieJsonNode.path("genreNm").asText());
            movieDTOBuilder.showTm(movieJsonNode.path("showTm").asText());
            movieDTOBuilder.openDt(movieJsonNode.path("openDt").asText());
            movieDTOBuilder.audiAcc(movieJsonNode.path("audiAcc").asText());
            movieDTOBuilder.audiCnt(movieJsonNode.path("audiCnt").asText());
            //이미지도 보내줌
            
            
            
            MovieDTO movieDTO = movieDTOBuilder.build();
            totalTodayBoxOfficeMovie.add(movieDTO);
        }
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
