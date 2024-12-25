package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.api.KOBISRequestService;
import com.cldhfleks2.moviehub.api.TMDBRequestService;
import com.cldhfleks2.moviehub.config.SeleniumWebDriver;
import com.cldhfleks2.moviehub.movie.*;
import com.cldhfleks2.moviehub.movie.audit.MovieAuditRepository;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStatRepository;
import com.cldhfleks2.moviehub.movie.genre.MovieGenreRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicService {
    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final SeleniumWebDriver seleniumWebDriver;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieAuditRepository movieAuditRepository;
    private final MovieDailyStatRepository movieDailyStatRepository;
    private final KOBISRequestService kobisRequestService;
    private final TMDBRequestService tmdbRequestService;

    @Value("${kobis.key}")
    private String kobiskey;

    String test(Model model) throws Exception {

        return "test";
    }

    String test2(Model model) throws Exception {

        return "test";
    }

    //날짜 가져오는 함수
    private String getCurrentDay() {
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter);
    }

    //메인 페이지 GET
    String getMain(Model model) throws Exception{
        //1. 전체 일일 박스 오피스 DTO
        HttpResponse<String> totalTodayBoxOfficeResponse = kobisRequestService.getTotalTodayBoxOfficeResponse();
        List<MovieDTO> totalTodayBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(totalTodayBoxOfficeResponse, "dailyBoxOfficeList");
        model.addAttribute("totalTodayBoxOfficeMovie", totalTodayBoxOfficeMovie);

        //2. 전체 주간 박스오피스
        HttpResponse<String> totalWeeklyBoxOfficeResponse = kobisRequestService.getTotalWeeklyBoxOfficeResponse();
        List<MovieDTO> totalWeeklyBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(totalWeeklyBoxOfficeResponse, "weeklyBoxOfficeList");
        model.addAttribute("totalWeeklyBoxOfficeMovie", totalWeeklyBoxOfficeMovie);

        //3. 주간 한국 박스오피스
        HttpResponse<String> koreaWeeklyBoxOfficeResponse = kobisRequestService.getKoreaWeeklyBoxOfficeResponse();
        List<MovieDTO> koreaWeeklyBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(koreaWeeklyBoxOfficeResponse, "weeklyBoxOfficeList");
        model.addAttribute("koreaWeeklyBoxOfficeMovie", koreaWeeklyBoxOfficeMovie);

        //4. 주간 외국 박스오피스
        HttpResponse<String> foreignWeeklyBoxOffice = kobisRequestService.getForeignWeeklyBoxOfficeResponse();
        List<MovieDTO> foreignWeeklyBoxOfficeMovie = movieService.getMovieDTOAsBoxOffice(foreignWeeklyBoxOffice, "weeklyBoxOfficeList");
        model.addAttribute("foreignWeeklyBoxOfficeMovie", foreignWeeklyBoxOfficeMovie);

        return "main/main";
    }

    //영화 상세 페이지 GET
    String getDetail(String movieCd, Model model, RedirectAttributes redirectAttributes) throws Exception{
        //1차 : DB 검색
        Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
        if(!movieObj.isPresent()){
            //2차 : KOBIS API 검색
            HttpResponse<String> movieDetailResponse =  kobisRequestService.sendMovieDetailRequest(movieCd);
            String movieDetailResponseBody = movieDetailResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode movieDetailJsonNode = objectMapper.readTree(movieDetailResponseBody);
            JsonNode movieCdNode = movieDetailJsonNode.path("movieInfoResult").path("movieInfo").path("movieCd");
            if(movieCdNode.isNull()){
                //그래도 없으면 메인페이지로 리다이렉트
                redirectAttributes.addFlashAttribute("alertMessage", "영화가 존재하지 않습니다.");
                return "redirect:/main";
            }
            
            String currentDay = getCurrentDay();
            JsonNode movieInfo = movieDetailJsonNode.path("movieInfoResult").path("movieInfo");
            //영화를 DB에 저장
            ReturnEntitysDTO returnEntitysDTO = movieService.saveEntityAsMovieDetail(movieInfo , currentDay);
            //모델에 MovieDTO 담기
            MovieDTO movieDetail = movieService.convertToMovieDTO(returnEntitysDTO);
            model.addAttribute("movieDetail", movieDetail);

            //모델에 영화의 좋아요 갯수 담기

            return "detail/detail";
        }else{
            //DB에있는 영화 정보를 보여준다.
            Movie movie = movieObj.get();
            Long movieId = movie.getId();
            MovieDTO movieDetail = movieService.getMovieDTO(movieId);
            model.addAttribute("movieDetail", movieDetail);

            //모델에 영화의 좋아요 갯수 담기

            return "detail/detail";
        }
    }


    //검색 페이지 GET
    String getSearch(String keyword, String category, Model model)  throws Exception{
        if(keyword == null) keyword = ""; //검색어 없을때
        if(category == null) category = ""; //검색 조건 : 영화제목, 배우/감독명

        if(keyword == "")
            return "search/search";
        
        //키워드로 검색 진행
        if(category.equals("movieName")){
            log.info("영화제목 검색시작");
            List<MovieDTO> movieList = movieService.searchMovieAsMovieName(keyword);
            model.addAttribute("movieList", movieList);
            log.info("영화제목으로 영화 리스트를 잘 갖고옴.");
        }else if(category.equals("moviePeople")){
            log.info("배우/감독명 검색시작");



        }

        log.info("페이지 전송, category = {}", category);
        return "search/search";
    }
}
