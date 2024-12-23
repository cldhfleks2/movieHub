package com.cldhfleks2.moviehub.movie;

import com.cldhfleks2.moviehub.KOBISRequestService;
import com.cldhfleks2.moviehub.TMDBRequestService;
import com.cldhfleks2.moviehub.config.ExcuteTask;
import com.cldhfleks2.moviehub.config.SeleniumWebDriverConfig;
import com.cldhfleks2.moviehub.movie.actor.MovieActor;
import com.cldhfleks2.moviehub.movie.actor.MovieActorRepository;
import com.cldhfleks2.moviehub.movie.audit.MovieAudit;
import com.cldhfleks2.moviehub.movie.audit.MovieAuditRepository;
import com.cldhfleks2.moviehub.movie.company.MovieCompany;
import com.cldhfleks2.moviehub.movie.company.MovieCompanyRepository;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStatRepository;
import com.cldhfleks2.moviehub.movie.director.MovieDirector;
import com.cldhfleks2.moviehub.movie.director.MovieDirectorRepository;
import com.cldhfleks2.moviehub.movie.genre.MovieGenre;
import com.cldhfleks2.moviehub.movie.genre.MovieGenreRepository;
import com.cldhfleks2.moviehub.movie.nation.MovieNation;
import com.cldhfleks2.moviehub.movie.nation.MovieNationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
//DB 저장을 담당
public class MovieService {
    private final MovieNationRepository movieNationRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieCompanyRepository movieCompanyRepository;
    private final MovieDailyStatRepository movieDailyStatRepository;
    private final MovieDirectorRepository movieDirectorRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieAuditRepository movieAuditRepository;
    private final MovieRepository movieRepository;
    private final SeleniumWebDriverConfig seleniumWebDriverConfig;
    private final KOBISRequestService kobisRequestService;
    private final TMDBRequestService tmdbRequestService;

    //날짜 가져오는 함수 : KOBIS에서 하루이전 통계만 잡히므로 하루 이전 날짜로 계산
    private String getCurrentDay() {
        //KOBIS에서 하루이전 통계만 잡히므로 하루 이전 날짜로 계산
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter); //현재 날짜  예) "20241220"
    }

    //실제 JsonNode를 탐색해서 Entity를 DB에 저장 하는 로직 : saveTodayBoxOfficeOnDB, saveWeeklyBoxOfficeOnDB에서 사용
    public void saveEntity(JsonNode boxOfficeList, String currentDay) throws Exception {
        //response에 존재하는 전체 영화 목록을 DB에 저장하는 로직
        for (int i = 0; i < boxOfficeList.size() && i < 10; i++) {
            JsonNode boxOfficeMovie = boxOfficeList.get(i); //현재 영화에 대한 처리
            String movieCd = boxOfficeMovie.path("movieCd").asText();
            String movieNm = boxOfficeMovie.path("movieNm").asText();
            String openDt = boxOfficeMovie.path("openDt").asText();
            String salesAcc = boxOfficeMovie.path("salesAcc").asText();
            String audiAcc = boxOfficeMovie.path("audiAcc").asText();
            String salesAmt = boxOfficeMovie.path("salesAmt").asText();
            String audiCnt = boxOfficeMovie.path("audiCnt").asText();
            String scrnCnt = boxOfficeMovie.path("scrnCnt").asText();
            String showCnt = boxOfficeMovie.path("showCnt").asText();
            //안쓰는건 주석처리 해둠 : rank, rnum, rankInten, rankOldAndNew, salesChange, salesShare, salesInten, audiChange

            //1. Movie객체가 존재할때
            Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
            if (movieObj.isPresent()) {
                //미공개 영화는 보여주지 않는다. : 예로들면 포스터 이미지를 가져오는데 실패하면 movie객체를 status=0으로 DB에 저장하게 해놧음
                if(movieObj.get().getStatus() == 0) continue;
                Movie movie = movieObj.get();
                Long movie_id = movie.getId();
                //Movie가 이미 존재 하면 관련 엔티티가 저장되며 dailyStat도 추가 되었겠지만 dailyStat은 매일 한번 업데이트 해줘야함.

                //2. 현재 날짜로 MovieDailyStat이 없을때
                Optional<MovieDailyStat> movieDailyStatObj = movieDailyStatRepository.findByDayAndMovieIdAndStatus(currentDay, movie_id);
                if (!movieDailyStatObj.isPresent()) {
                    //하루가 지난것이므로 새로이 갱신
                    List<MovieDailyStat> movieDailyStatList = movieDailyStatRepository.findByMovieIdAndStatus(movie_id);
                    //3. movie_id와 일치하는 MovieDailyStat이 존재 할때
                    if (!movieDailyStatList.isEmpty()) {
                        MovieDailyStat movieDailyStat = movieDailyStatList.get(movieDailyStatList.size() - 1); //가장 최근것
                        movieDailyStat.setDay(currentDay); //현재 날짜로 수정
                        movieDailyStatRepository.save(movieDailyStat);
                    } //3. movie_id에 일치하는 movieDailyStat이 없다..? => Movie객체를 저장할때 같이 저장해야하는데 에러가 낫던것.
                    //여기에 추가로 저장하면 DB에 중복저장되는 에러가 있어서 이런 경우는 무시하겠음
                } //2. 현재 날짜인 MovieDailyStat이 존재 할때 : 저장할 필요가없으니 끝
            } else {
                //1. Movie객체가 없을때
                Movie movie = new Movie(); //Movie 엔티티
                movie.setMovieCd(movieCd);
                movie.setMovieNm(movieNm);
                movie.setOpenDt(openDt);
                movie.setSalesAcc(salesAcc);
                movie.setAudiAcc(audiAcc);
                //포스터 가져오기
                String posterURL = tmdbRequestService.getMoviePosterURL(movieNm); //새로운 기능. 영화이름으로 검색
                if(posterURL == null){
                    movie.setPosterURL("null");
                    movie.setStatus(0); //미공개 영화로 저장
                    movieRepository.save(movie);
                    continue; //다른 엔티티를 저장하지 않음.
                }
                movie.setPosterURL(posterURL);

                //영화의 상세 정보를 받기 새로운 KOBIS API 요청을 날림
                HttpResponse<String> movieDetailResponse = kobisRequestService.sendMovieDetailRequest(movieCd);
                String movieDetailResponseBody = movieDetailResponse.body();
                ObjectMapper movieDetailObjectMapper = new ObjectMapper();
                JsonNode movieDetailJsonNode = movieDetailObjectMapper.readTree(movieDetailResponseBody);
                JsonNode movieDetail = movieDetailJsonNode.path("movieInfoResult").path("movieInfo");
                String movieNmEn = movieDetail.path("movieNmEn").asText();
                String showTm = movieDetail.path("showTm").asText();
                String prdtYear = movieDetail.path("prdtYear").asText();
                String typeNm = movieDetail.path("typeNm").asText();
                //안쓰는것들은 주석 처리함 : movieNmOg, prdtStatNm, showTypes, staffs

                movie.setMovieNmEn(movieNmEn);
                movie.setShowTm(showTm);
                movie.setPrdtYear(prdtYear);
                movie.setTypeNm(typeNm);
                movieRepository.save(movie); //가장 먼저 저장해줘야 다른 엔티티들이 외래키로 사용가능

                MovieDailyStat movieDailyStat = new MovieDailyStat(); //MovieDailyStat 엔티티
                movieDailyStat.setDay(currentDay); //"20241220"
                movieDailyStat.setSalesAmt(salesAmt);
                movieDailyStat.setAudiCnt(audiCnt);
                movieDailyStat.setScrnCnt(scrnCnt);
                movieDailyStat.setShowCnt(showCnt);
                movieDailyStat.setMovie(movie); //외래키로 사용
                movieDailyStatRepository.save(movieDailyStat);

                //나머지 엔티티들 추가
                JsonNode nationsList = movieDetail.path("nations");
                for (int j = 0; j < nationsList.size(); j++) {
                    MovieNation movieNation = new MovieNation();
                    movieNation.setNationNm(nationsList.get(j).path("nationNm").asText());
                    movieNation.setMovie(movie); //외래키로 사용
                    movieNationRepository.save(movieNation);
                }
                JsonNode genresList = movieDetail.path("genres");
                for (int j = 0; j < genresList.size(); j++) {
                    MovieGenre movieGenre = new MovieGenre();
                    movieGenre.setGenreNm(genresList.get(j).path("genreNm").asText());
                    movieGenre.setMovie(movie); //외래키로 사용
                    movieGenreRepository.save(movieGenre);
                }
                JsonNode directorsList = movieDetail.path("directors");
                for (int j = 0; j < directorsList.size(); j++) {
                    MovieDirector movieDirector = new MovieDirector();
                    movieDirector.setPeopleNm(directorsList.get(j).path("peopleNm").asText());
                    movieDirector.setPeopleNmEn(directorsList.get(j).path("peopleNmEn").asText());
                    movieDirector.setMovie(movie); //외래키로 사용
                    movieDirectorRepository.save(movieDirector);
                }
                JsonNode actorsList = movieDetail.path("actors");
                for (int j = 0; j < actorsList.size(); j++) {
                    MovieActor movieActor = new MovieActor();
                    movieActor.setPeopleNm(actorsList.get(j).path("peopleNm").asText());
                    movieActor.setPeopleNmEn(actorsList.get(j).path("peopleNmEn").asText());
                    movieActor.setMovie(movie); //외래키로 사용
                    movieActorRepository.save(movieActor);
                }
                JsonNode companysList = movieDetail.path("companys");
                for (int j = 0; j < companysList.size(); j++) {
                    MovieCompany movieCompany = new MovieCompany();
                    movieCompany.setCompanyCd(companysList.get(j).path("").asText());
                    movieCompany.setCompanyNm(companysList.get(j).path("companyNm").asText());
                    movieCompany.setCompanyNmEn(companysList.get(j).path("companyNmEn").asText());
                    movieCompany.setCompanyPartNm(companysList.get(j).path("companyPartNm").asText());
                    movieCompany.setMovie(movie); //외래키로 사용
                    movieCompanyRepository.save(movieCompany);
                }
                JsonNode auditsList = movieDetail.path("audits");
                for (int j = 0; j < auditsList.size(); j++) {
                    MovieAudit movieAudit = new MovieAudit();
                    movieAudit.setWatchGradeNm(auditsList.get(j).path("watchGradeNm").asText());
                    movieAudit.setMovie(movie); //외래키로 사용
                    movieAuditRepository.save(movieAudit);
                }
            }
        }
    }

    //KOBIS에서 주간/주말박스오피스 결과로 받은 response을 파싱해서 Entity를 DB에 추가하는 함수
    public void saveWeeklyBoxOfficeOnDB(HttpResponse<String> weeklyBoxOfficeResponse) throws Exception{
        if(weeklyBoxOfficeResponse == null){
            log.error("saveWeeklyBoxOfficeOnDB 함수로 null값이 전달 되었음.");
            return;
        }
        String currentDay = getCurrentDay();

        //JSON파싱 진행
        String weeklyBoxOfficeResponseBody = weeklyBoxOfficeResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(weeklyBoxOfficeResponseBody);
        JsonNode weeklyBoxOfficeList = jsonNode.path("boxOfficeResult").path("weeklyBoxOfficeList");

        //실제로 JsonNode를 탐색해서 Entity를 DB에 저장 하는 로직
        saveEntity(weeklyBoxOfficeList, currentDay);
    }

    //KOBIS에서 일별박스오피스 결과로 받은 response을 파싱해서 Entity를 DB에 추가하는 함수
    public void saveTodayBoxOfficeOnDB(HttpResponse<String> todayBoxOfficeResponse) throws Exception {
        if(todayBoxOfficeResponse == null){
            log.error("saveTodayBoxOfficeOnDB 함수로 null값이 전달 되었음.");
            return;
        }
        String currentDay = getCurrentDay(); //현재 날짜  예) "20241220"

        //JSON파싱 진행
        String todayBoxOfficeResponseBody = todayBoxOfficeResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(todayBoxOfficeResponseBody);
        JsonNode todayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");
        String day = jsonNode.path("boxOfficeResult").path("showRange").asText().split("~")[0]; //날짜표기중 ~ 기준으로 앞의 날짜만 가져옴    예) "20241220"

        //현재 날짜와 totalTodayBoxOfficeResponse의 날짜가 다르면 하루가 지나서 새로 갱신해야함.
        if (!currentDay.equals(day)) {
            todayBoxOfficeResponse = kobisRequestService.sendTotalTodayBoxOfficeRequest(); //새로 요청하고 응답 받음
            todayBoxOfficeResponseBody = todayBoxOfficeResponse.body();
            jsonNode = objectMapper.readTree(todayBoxOfficeResponseBody);
            todayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");
        }

        //실제로 JsonNode를 탐색해서 Entity를 DB에 저장 하는 로직
        saveEntity(todayBoxOfficeList, currentDay);
    }
    
    //검색 페이지나 상세페이지(아직 DB에없는 movieCd로 상세페이지 접근시)에서 DB에 영화를 저장하는 함수
    public void saveMovie(){

    }

}
