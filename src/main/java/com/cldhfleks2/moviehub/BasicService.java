package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.api.KOBISRequestService;
import com.cldhfleks2.moviehub.api.TMDBRequestService;
import com.cldhfleks2.moviehub.config.SeleniumWebDriver;
import com.cldhfleks2.moviehub.movie.*;
import com.cldhfleks2.moviehub.movie.audit.MovieAuditRepository;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStatRepository;
import com.cldhfleks2.moviehub.movie.genre.MovieGenreRepository;
import com.cldhfleks2.moviehub.movie.people.PeopleDTO;
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
        //DB에 없으면 DB에 추가하고 보여줌
        Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
        if(!movieObj.isPresent()){
            //KOBIS API 영화 상세 검색
            HttpResponse<String> movieDetailResponse =  kobisRequestService.sendMovieDetailRequest(movieCd);
            if(movieDetailResponse == null){
                log.error("getDetail movieDetailResponse 요청 실패!!!!");
                //에러 나서 메인페이지로 리다이렉트
                redirectAttributes.addFlashAttribute("alertMessage", "알 수 없는 에러가 발생했습니다.");
                return "redirect:/main";
            }
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
            if(returnEntitysDTO == null){
                //영화 포스터가 없으면 returnEntitysDTO가 null값으로 전달됨
                redirectAttributes.addFlashAttribute("alertMessage", "영화가 존재하지 않습니다.");
                return "redirect:/main";
            }

            //모델에 MovieDTO 담기
            MovieDTO movieDetail = movieService.convertToMovieDTO(returnEntitysDTO);
            model.addAttribute("movieDetail", movieDetail);

            //모델에 영화의 좋아요 갯수 담기

            return "detail/detail";
        }else{
            //DB에 있으면 그대로 영화 정보를 보여준다.
            Movie movie = movieObj.get();
            Long movieId = movie.getId();
            String currentDay = getCurrentDay();

            //MovieDailyStat은 아래 getMovieDTO에서 진행
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
        if(category.equals("movieName")){ //KOBIS API 사용
            log.info("영화제목으로 영화 목록 검색 시작");
            List<MovieDTO> movieList = movieService.searchMovieAsMovieName(keyword, 50); //영화 검색결과를 가져옴 갯수제한
            model.addAttribute("movieList", movieList);
            log.info("영화제목으로 영화 목록을  갖고옴.");
            
        }else if(category.equals("moviePeople")){ //TMDB API 사용
            log.info("배우/감독명 검색 시작");
            List<PeopleDTO> peopleDTOList = movieService.searchProfileAsPeopleName(keyword); //프로필이미지만 가져옴
            model.addAttribute("peopleDTOList", peopleDTOList);
            log.info("배우/감독명으로 프로필들을 갖고옴.");
            
        }else if(category.equals("peopleClick")){
            log.info("해당 인물에 대한 영화 목록 검색 시작");
            //프로필을 클릭했을때 그 사람의 영화 목록을 줌
            //이때 키워드는 peopleId를 전달 해줌.
            Long peopleId = Long.parseLong(keyword);
            List<MovieDTO> movieList = movieService.searchMovieAsPeopleId(peopleId, 50); //검색결과 최대 8개 제한
            model.addAttribute("movieList", movieList);
            log.info("해당 인물에 대한 영화 목록을 갖고옴.");
        }

        log.info("페이지 전송, category = {}", category);
        return "search/search";
    }
}
