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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicService {
    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final SeleniumWebDriverConfig seleniumWebDriverConfig;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieAuditRepository movieAuditRepository;
    private final MovieDailyStatRepository movieDailyStatRepository;
    private final KOBISRequestService kobisRequestService;

    @Value("${kobis.key}")
    private String kobiskey;

    String test(Model model) throws Exception {

        return "test";
    }

    String test2(Model model) throws Exception {

        return "test";
    }

    //jsonNodeList를 파싱해서 MovieDTO를 생성 해서 리턴해줌
    List<MovieDTO> getMovieDTO(JsonNode jsonNodeList) throws Exception {

        //response에서 영화 목록만을 참고하려함. 실제 영화 데이터는 이미 DB에 존재할것.
        List<MovieDTO> movieDTOList = new ArrayList<>();
        for (int i = 0; i < jsonNodeList.size() && i < 10; i++) {
            //영화 목록중 현재 영화에 대한 처리
            JsonNode movieJsonNode = jsonNodeList.get(i);
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
                log.error("{} 영화 객체와 관련된 MovieGenre객체에 문제가 있음", movie);
                continue;
            }
            movieDTOBuilder.genreList(movieGenreList);
            //3-2. 시청 가이드라인 가져오기 (MovieAudit에서)
            List<MovieAudit> movieAuditList = movieAuditRepository.findByMovieIdAndStatus(movieId);
            if(movieAuditList.isEmpty()){
                log.error("{} 영화 객체와 관련된 MovieAudit객체에 문제가 있음", movie);
                continue;
            }
            movieDTOBuilder.watchGradeList(movieAuditList);
            //3-3. 관객수 가져오기 (MovieDailyStat에서)
            List<MovieDailyStat> movieDailyStatList = movieDailyStatRepository.findByMovieIdAndStatus(movieId);
            if(movieDailyStatList.isEmpty()){
                log.error("{} 영화 객체와 관련된 MovieDailyStat객체에 문제가 있음", movie);
                continue;
            }
            movieDTOBuilder.audiCnt(movieDailyStatList.get(0).getAudiCnt()); //값만 보냄

            //4. 마지막으로 결과를 담음
            MovieDTO movieDTO = movieDTOBuilder.build();
            movieDTOList.add(movieDTO);
        }
        return movieDTOList;
    }



    //메인 페이지 GET
    String getMain(Model model) throws Exception{
        //1. 전체 일일 박스 오피스 DTO
        HttpResponse<String> totalTodayBoxOfficeResponse = kobisRequestService.getTotalTodayBoxOfficeResponse();
        String totalTodayBoxOfficeResponseBody = totalTodayBoxOfficeResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(totalTodayBoxOfficeResponseBody);
        JsonNode totalTodayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");
        List<MovieDTO> totalTodayBoxOfficeMovie = getMovieDTO(totalTodayBoxOfficeList);
        model.addAttribute("totalTodayBoxOfficeMovie", totalTodayBoxOfficeMovie);

        //2. 전체 주간 박스오피스
        HttpResponse<String> totalWeeklyBoxOfficeResponse = kobisRequestService.getTotalWeeklyBoxOfficeResponse();
        String totalWeeklyBoxOfficeResponseBody = totalWeeklyBoxOfficeResponse.body();
        objectMapper = new ObjectMapper();
        jsonNode = objectMapper.readTree(totalWeeklyBoxOfficeResponseBody);
        JsonNode totalWeeklyBoxOfficeList = jsonNode.path("boxOfficeResult").path("weeklyBoxOfficeList");
        List<MovieDTO> totalWeeklyBoxOfficeMovie = getMovieDTO(totalWeeklyBoxOfficeList);
        model.addAttribute("totalWeeklyBoxOfficeMovie", totalWeeklyBoxOfficeMovie);

        //3. 주간 한국 박스오피스
        HttpResponse<String> koreaWeeklyBoxOfficeResponse = kobisRequestService.getKoreaWeeklyBoxOfficeResponse();
        String koreaWeeklyBoxOfficeResponseBody = koreaWeeklyBoxOfficeResponse.body();
        objectMapper = new ObjectMapper();
        jsonNode = objectMapper.readTree(koreaWeeklyBoxOfficeResponseBody);
        JsonNode koreaWeeklyBoxOfficeList = jsonNode.path("boxOfficeResult").path("weeklyBoxOfficeList");
        List<MovieDTO> koreaWeeklyBoxOfficeMovie = getMovieDTO(koreaWeeklyBoxOfficeList);
        model.addAttribute("koreaWeeklyBoxOfficeMovie", koreaWeeklyBoxOfficeMovie);

        //4. 주간 외국 박스오피스
        HttpResponse<String> foreignWeeklyBoxOffice = kobisRequestService.getForeignWeeklyBoxOfficeResponse();
        String foreignWeeklyBoxOfficeBody = foreignWeeklyBoxOffice.body();
        objectMapper = new ObjectMapper();
        jsonNode = objectMapper.readTree(foreignWeeklyBoxOfficeBody);
        JsonNode foreignWeeklyBoxOfficeList = jsonNode.path("boxOfficeResult").path("weeklyBoxOfficeList");
        List<MovieDTO> foreignWeeklyBoxOfficeMovie = getMovieDTO(foreignWeeklyBoxOfficeList);
        model.addAttribute("foreignWeeklyBoxOfficeMovie", foreignWeeklyBoxOfficeMovie);


        return "main/main";
    }

    //날짜 가져오는 함수
    private String getCurrentDay() {
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter);
    }

    String getDetail(String movieCd, Model model, RedirectAttributes redirectAttributes) throws Exception{
        //1차 : DB 검색
        Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
        if(!movieObj.isPresent()){
            //2차 : KOBIS API 검색
            HttpResponse<String> movieDetailResponse =  kobisRequestService.sendMovieDetailRequest(movieCd);
            String movieDetailResponseBody = movieDetailResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode movieDetailJsonNode = objectMapper.readTree(movieDetailResponseBody);
            JsonNode movieDetail = movieDetailJsonNode.path("movieInfoResult").path("movieInfo").path("movieCd");
            if(movieDetail.isNull()){
                //그래도 없으면 메인페이지로 리다이렉트
                redirectAttributes.addFlashAttribute("alertMessage", "영화가 존재하지 않습니다.");
                return "redirect:/main";
            }
            
            //영화를 DB에 저장하고 그 정보를 보여주면됌
            String currentDay = getCurrentDay();
            JsonNode movieInfo = movieDetailJsonNode.path("movieInfoResult").path("movieInfo");
            movieService.saveEntityAsMovieDetailResponse(movieInfo , currentDay);


        }








        return "detail/detail";
    }


}
