package com.cldhfleks2.moviehub.movie;

import com.cldhfleks2.moviehub.api.KOBISRequestService;
import com.cldhfleks2.moviehub.api.TMDBRequestService;
import com.cldhfleks2.moviehub.config.SeleniumWebDriver;
import com.cldhfleks2.moviehub.error.ErrorService;
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
import com.cldhfleks2.moviehub.movie.people.PeopleDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.net.URLEncoder;
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
    private final KOBISRequestService kobisRequestService;
    private final TMDBRequestService tmdbRequestService;

    //날짜 가져오는 함수 : KOBIS에서 하루이전 통계만 잡히므로 하루 이전 날짜로 계산
    private String getCurrentDay() {
        //KOBIS에서 하루이전 통계만 잡히므로 하루 이전 날짜로 계산
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter); //현재 날짜  예) "20241220"
    }

    // URL 안전한 문자열로 인코딩 (UTF-8)
    public String encodeString(String keyword) throws Exception {
        return URLEncoder.encode(keyword, "UTF-8");
    }

    //MovieId 로 MovieDTO를 생성
    //동시에 MovieDailyStat을 갱신
    public MovieDTO getMovieDTO(Long movieId) {
        Optional<Movie> movieObj = movieRepository.findById(movieId);
        if(!movieObj.isPresent()){ //존재하지않는 movieId
            ErrorService.send(HttpStatus.NOT_FOUND.value(), "getMovieDTO method", "존재 하지 않는 movieId를 사용함", void.class);
            return null;
        }
        Movie movie = movieObj.get();
        List<MovieGenre> genreList = movieGenreRepository.findByMovieIdAndStatus(movieId);
        List<MovieAudit> auditList = movieAuditRepository.findByMovieIdAndStatus(movieId);
        String currentDay = getCurrentDay();
        MovieDailyStat movieDailyStat = new MovieDailyStat();
        Optional<MovieDailyStat> movieDailyStatObj = movieDailyStatRepository.findByDayAndMovieIdAndStatus(currentDay, movieId);
        if(movieDailyStatObj.isPresent()) { //오늘 날짜가 있는지
            movieDailyStat = movieDailyStatObj.get();
        }else { //이전 날짜가 있는지
            List<MovieDailyStat> movieDailyStatList = movieDailyStatRepository.findByMovieIdAndStatus(movieId);
            if(!movieDailyStatList.isEmpty()){ //이전 날짜가 있으면 오늘 날짜로 업데이트하고 이 함수에서 계속 사용
                movieDailyStat = movieDailyStatList.get(movieDailyStatList.size()-1); //가장 최근것
                movieDailyStat.setDay(currentDay); //오늘 날짜로 수정
                movieDailyStatRepository.save(movieDailyStat); //수정해서 저장
            }else{
                //이전 날짜도 없으면 Movie를 저장할때부터 에러가 난것임.
            }
        }
        List<MovieDirector> directorList = movieDirectorRepository.findByMovieIdAndStatus(movieId);
        List<MovieActor> actorList = movieActorRepository.findByMovieIdAndStatus(movieId);

        MovieDTO movieDTO = MovieDTO.builder()
                .movieCd(movie.getMovieCd())
                .movieNm(movie.getMovieNm())
                .movieNmEn(movie.getMovieNmEn())
                .genreList(genreList)
                .auditList(auditList)
                .showTm(movie.getShowTm())
                .openDt(movie.getOpenDt())
                .audiAcc(movie.getAudiAcc())
                .audiCnt(movieDailyStat.getAudiCnt())
                .posterURL(movie.getPosterURL())
                .directorList(directorList)
                .actorList(actorList)
                .build();

        return movieDTO;
    }

    //ReturnEntitysDTO(영화관련묶음엔티티) => MovieDTO
    public MovieDTO convertToMovieDTO(ReturnEntitysDTO returnEntitysDTO) {
        MovieDTO movieDTO = MovieDTO.builder()
                .movieCd(returnEntitysDTO.getMovie().getMovieCd())
                .movieNm(returnEntitysDTO.getMovie().getMovieNm())
                .movieNmEn(returnEntitysDTO.getMovie().getMovieNmEn())
                .genreList(returnEntitysDTO.getMovieGenreList())
                .auditList(returnEntitysDTO.getMovieAuditList())
                .showTm(returnEntitysDTO.getMovie().getShowTm())
                .openDt(returnEntitysDTO.getMovie().getOpenDt())
                .audiAcc(returnEntitysDTO.getMovie().getAudiAcc())
                .audiCnt(returnEntitysDTO.getMovieDailyStat().getAudiCnt())
                .posterURL(returnEntitysDTO.getMovie().getPosterURL())
                .build();

        return movieDTO;
    }

    //영화 상세 정보를 DB에 저장 : movieInfo가 정상적으로 있는지 확인은 상위 함수에서 처리했을거임. 저장만 하는함수
    public ReturnEntitysDTO saveEntityAsMovieDetail(JsonNode movieInfo, String currentDay) throws Exception {
        String movieCd = movieInfo.get("movieCd").asText();
        String movieNm = movieInfo.get("movieNm").asText();
        String openDt = movieInfo.get("openDt").asText();
        String movieNmEn = movieInfo.get("movieNmEn").asText();
        String prdtYear = movieInfo.get("prdtYear").asText();
        String showTm = movieInfo.get("showTm").asText();
        String typeNm = movieInfo.get("typeNm").asText();

        Movie movie = new Movie(); //Movie 엔티티
        movie.setMovieCd(movieCd);
        movie.setMovieNm(movieNm);
        movie.setOpenDt(openDt);
        movie.setMovieNmEn(movieNmEn);
        movie.setShowTm(showTm);
        movie.setPrdtYear(prdtYear);
        movie.setTypeNm(typeNm);
        //포스터 가져오기 : 3차에 걸쳐서 시도함
        String posterURL = tmdbRequestService.getMoviePostURL(movieCd, movieNm, movieNmEn);
        if(posterURL == null){
            movie.setPosterURL("null");
            movie.setStatus(0); //미공개 영화로 저장
            movieRepository.save(movie);
            return null; //다른 엔티티를 저장하지 않음.
        }

        movie.setPosterURL(posterURL);
        movieRepository.save(movie); //외래키로 사용하기위해 먼저 저장

        MovieDailyStat movieDailyStat = new MovieDailyStat(); //MovieDailyStat 엔티티
        movieDailyStat.setDay(currentDay); //"20241220"
        //이것은 영화 상세 정보에는 없는 값들.
//        movieDailyStat.setSalesAmt(salesAmt);
//        movieDailyStat.setAudiCnt(audiCnt);
//        movieDailyStat.setScrnCnt(scrnCnt);
//        movieDailyStat.setShowCnt(showCnt);
        movieDailyStat.setMovie(movie); //외래키로 사용
        movieDailyStatRepository.save(movieDailyStat);


        //나머지 엔티티들 추가
        JsonNode nationsList = movieInfo.path("nations");
        List<MovieNation> movieNationList = new ArrayList<>();
        for (int j = 0; j < nationsList.size(); j++) {
            MovieNation movieNation = new MovieNation();
            movieNation.setNationNm(nationsList.get(j).path("nationNm").asText());
            movieNation.setMovie(movie); //외래키로 사용
            movieNationRepository.save(movieNation);
            movieNationList.add(movieNation);
        }
        JsonNode genresList = movieInfo.path("genres");
        List<MovieGenre> movieGenreList = new ArrayList<>();
        for (int j = 0; j < genresList.size(); j++) {
            MovieGenre movieGenre = new MovieGenre();
            movieGenre.setGenreNm(genresList.get(j).path("genreNm").asText());
            movieGenre.setMovie(movie); //외래키로 사용
            movieGenreRepository.save(movieGenre);
            movieGenreList.add(movieGenre);
        }
        JsonNode directorsList = movieInfo.path("directors");
        List<MovieDirector> movieDirectorList = new ArrayList<>();
        for (int j = 0; j < directorsList.size(); j++) {
            MovieDirector movieDirector = new MovieDirector();
            movieDirector.setPeopleNm(directorsList.get(j).path("peopleNm").asText());
            movieDirector.setPeopleNmEn(directorsList.get(j).path("peopleNmEn").asText());
            movieDirector.setMovie(movie); //외래키로 사용
            movieDirectorRepository.save(movieDirector);
            movieDirectorList.add(movieDirector);
        }
        JsonNode actorsList = movieInfo.path("actors");
        List<MovieActor> movieActorList = new ArrayList<>();
        for (int j = 0; j < actorsList.size(); j++) {
            MovieActor movieActor = new MovieActor();
            movieActor.setPeopleNm(actorsList.get(j).path("peopleNm").asText());
            movieActor.setPeopleNmEn(actorsList.get(j).path("peopleNmEn").asText());
            movieActor.setMovie(movie); //외래키로 사용
            movieActorRepository.save(movieActor);
            movieActorList.add(movieActor);
        }
        JsonNode companysList = movieInfo.path("companys");
        List<MovieCompany> movieCompanyList = new ArrayList<>();
        for (int j = 0; j < companysList.size(); j++) {
            MovieCompany movieCompany = new MovieCompany();
            movieCompany.setCompanyCd(companysList.get(j).path("").asText());
            movieCompany.setCompanyNm(companysList.get(j).path("companyNm").asText());
            movieCompany.setCompanyNmEn(companysList.get(j).path("companyNmEn").asText());
            movieCompany.setCompanyPartNm(companysList.get(j).path("companyPartNm").asText());
            movieCompany.setMovie(movie); //외래키로 사용
            movieCompanyRepository.save(movieCompany);
            movieCompanyList.add(movieCompany);
        }
        JsonNode auditsList = movieInfo.path("audits");
        List<MovieAudit> movieAuditList = new ArrayList<>();
        for (int j = 0; j < auditsList.size(); j++) {
            MovieAudit movieAudit = new MovieAudit();
            movieAudit.setWatchGradeNm(auditsList.get(j).path("watchGradeNm").asText());
            movieAudit.setMovie(movie); //외래키로 사용
            movieAuditRepository.save(movieAudit);
            movieAuditList.add(movieAudit);
        }

        ReturnEntitysDTO returnEntitysDTO = ReturnEntitysDTO.builder()
                .movie(movie)
                .movieDailyStat(movieDailyStat)
                .movieDirectorList(movieDirectorList)
                .movieActorList(movieActorList)
                .movieNationList(movieNationList)
                .movieGenreList(movieGenreList)
                .movieCompanyList(movieCompanyList)
                .movieAuditList(movieAuditList)
                .build();

        return returnEntitysDTO;
    }

    //박스오피스 목록을 DB에 저장 : 실제 JsonNode를 탐색하여 저장
    //saveTodayBoxOfficeOnDB, saveWeeklyBoxOfficeOnDB 에서 사용
    public void saveEntityAsBoxOffice(JsonNode boxOfficeList, String currentDay) throws Exception {
        //response에 존재하는 전체 영화 목록을 DB에 저장하는 로직
        for (int i = 0; i < boxOfficeList.size() && i < 10; i++) {
            JsonNode boxOfficeMovie = boxOfficeList.get(i); //현재 영화에 대한 처리
            String movieCd = boxOfficeMovie.path("movieCd").asText();
            String movieNm = boxOfficeMovie.path("movieNm").asText();
            String openDt = boxOfficeMovie.path("openDt").asText();
            //박스 오피스 목록에서만 구할 수 있는 값들 => 영화 상세 정보 response를 사용시 전부 빈 문자열이 들어갈것임.
            String salesAcc = boxOfficeMovie.path("salesAcc").asText(); //누적 기록
            String audiAcc = boxOfficeMovie.path("audiAcc").asText(); //누적 기록
            String salesAmt = boxOfficeMovie.path("salesAmt").asText(); //일일 기록
            String audiCnt = boxOfficeMovie.path("audiCnt").asText(); //일일 기록
            String scrnCnt = boxOfficeMovie.path("scrnCnt").asText();
            String showCnt = boxOfficeMovie.path("showCnt").asText();

            //1. Movie객체가 존재할때
            Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
            if (movieObj.isPresent()) {
                //미공개 영화는 보여주지 않는다. : 예로들면 포스터 이미지를 가져오는데 실패하면 movie객체를 status=0으로 DB에 저장하게 해놧음
                if(movieObj.get().getStatus() == 0)
                    continue;

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
                        //값들 업데이트
                        movieDailyStat.setSalesAmt(salesAmt);
                        movieDailyStat.setAudiCnt(audiCnt);
                        movieDailyStat.setScrnCnt(scrnCnt);
                        movieDailyStat.setShowCnt(showCnt);
                        movieDailyStat.setMovie(movie); //외래키로 사용
                        movieDailyStatRepository.save(movieDailyStat);
                    } //3. movie_id에 일치하는 movieDailyStat이 없다..? => Movie객체를 저장할때 같이 저장해야하는데 에러가 낫던것.
                    //여기에 추가로 저장하면 DB에 중복저장되는 에러가 있어서 이런 경우는 무시하겠음
                } //2. 현재 날짜인 MovieDailyStat이 존재 할때 : 저장할 필요가없으니 끝

                //박스오피스 전용 값들을 업데이트 해줘야함.
                //movie 정보 업데이트
                movie.setSalesAcc(salesAcc);
                movie.setAudiAcc(audiAcc);
                movieRepository.save(movie); //수정


            } else {
                //1. Movie객체가 없을때
                Movie movie = new Movie(); //Movie 엔티티
                movie.setMovieCd(movieCd);
                movie.setMovieNm(movieNm);
                movie.setOpenDt(openDt);
                movie.setSalesAcc(salesAcc);
                movie.setAudiAcc(audiAcc);

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
                movie.setMovieNmEn(movieNmEn);
                movie.setShowTm(showTm);
                movie.setPrdtYear(prdtYear);
                movie.setTypeNm(typeNm);

                //포스터 가져오기
                String posterURL = tmdbRequestService.getMoviePostURL(movieCd, movieNm, movieNmEn);
                if(posterURL == null){
                    movie.setPosterURL("null");
                    movie.setStatus(0); //미공개 영화로 저장
                    movieRepository.save(movie);
                    continue; //다른 엔티티를 저장하지 않음.
                }

                movie.setPosterURL(posterURL);
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
        saveEntityAsBoxOffice(weeklyBoxOfficeList, currentDay);
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
        saveEntityAsBoxOffice(todayBoxOfficeList, currentDay);
    }




    //KOBIS API의 박스 오피스 데이터로 MovieDTO를 만드는 함수
    //이때 박스오피스 데이터는 파싱해서 DB에 저장되있다. (앱실행시나)
    //response에서 영화 이름들만 참고한다. 실제 영화 데이터는 DB에서 가져옴
    public List<MovieDTO> getMovieDTOAsBoxOffice(HttpResponse<String> response, String boxOfficeList) throws Exception {
        String boxOfficeResponseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(boxOfficeResponseBody);
        JsonNode jsonNodeList = jsonNode.path("boxOfficeResult").path(boxOfficeList);

        List<MovieDTO> movieDTOList = new ArrayList<>();
        //영화 목록중 현재 영화에 대한 처리
        for (int i = 0; i < jsonNodeList.size() && i < 10; i++) {
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
            movieDTOBuilder.auditList(movieAuditList);
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

    //KOBIS API의 영화 목록으로 MovieDTO를 만드는 함수
    //search뷰에 필요한 정보만 보여줌 저장X
    public List<MovieDTO> getMovieDTOAsMovieList(HttpResponse<String> response, int limit) throws Exception {
        //영화목록response을 파싱해서 나온 JsonNode로 MovieDTO를 생성
        String movieListResponseBody = response.body();
        if (response == null) {
            log.error("getMovieDTOAsMovieList 에러 발생!: {}", movieListResponseBody);
            return null;
        }
        log.info("getMovieDTOAsMovieList responseBody: {}", movieListResponseBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(movieListResponseBody);
        JsonNode jsonNodeList = jsonNode.path("movieListResult").path("movieList");

        List<MovieDTO> movieDTOList = new ArrayList<>();

        if(!jsonNodeList.isArray() || jsonNodeList.size() == 0){ //KOBIS에 없는 영화일경우 지금 까지 아는정보만으로 보여줌
            return null;
        }

        //영화 목록중 현재 영화에 대한 처리
        int totalLimit = Math.min(jsonNodeList.size(), limit); //최대 갯수를 지정
        for (int i = 0; i < totalLimit; i++) {
            JsonNode movieJsonNode = jsonNodeList.get(i);
            String movieCd = movieJsonNode.path("movieCd").asText();
            String movieNm = movieJsonNode.path("movieNm").asText();
            String movieNmEn = movieJsonNode.path("movieNmEn").asText();
            String openDt = movieJsonNode.path("openDt").asText();
//            String prdtYear = movieJsonNode.path("prdtYear").asText(); //안쓰는값이라 주석
//            String typeNm = movieJsonNode.path("typeNm").asText(); //안쓰는값이라 주석
            // 하나라도 빈 값이 있을 때 실행할 코드
            if (movieCd.isEmpty() || movieNm.isEmpty() || movieNmEn.isEmpty() || openDt.isEmpty())
                continue;

            //평점 가져오기 (소숫점 1자리까지만)
            String average = "not-found";
            String primaryReleaseYear = openDt.substring(0, 4);
            //1차: 영화이름과 개봉년도로 검색
            HttpResponse<String> averageResponse = tmdbRequestService.sendSearchMovieAsMovieNmAndYear(movieNm, primaryReleaseYear);
            String averageResponseBody = averageResponse.body();
            objectMapper = new ObjectMapper();
            JsonNode averageJsonNode = objectMapper.readTree(averageResponseBody);
            JsonNode averageNodeList = averageJsonNode.path("results");
            //2차 : 개봉년도를 제외하고 다시 검색
            if (!(averageNodeList.isArray() && averageNodeList.size() > 0)) {
                averageResponse =tmdbRequestService.sendSearchMovieAsMovieNm(movieNm);
                averageResponseBody = averageResponse.body();
                objectMapper = new ObjectMapper();
                averageJsonNode = objectMapper.readTree(averageResponseBody);
                averageNodeList = averageJsonNode.path("results");
            }
            //검색결과를 파싱해서 average추출
            average = "not-found"; // 기본값을 "not-found"로 설정
            if (averageNodeList.isArray() && averageNodeList.size() > 0) {
                JsonNode firstMovie = averageNodeList.get(0); // 첫 번째 영화 객체 가져오기
                JsonNode averageNode = firstMovie.path("vote_average"); // vote_average 값 추출

                // "vote_average"가 null이 아니고 비어있지 않으면, 평점 값을 처리
                if (!averageNode.isNull() && !averageNode.asText().isEmpty()) {
                    double voteAverage = averageNode.asDouble(); // "vote_average" 값을 double로 가져오기
                    // 0.00 ~ 10.00 범위를 0.0 ~ 5.0 범위로 변환
                    double convertedAverage = voteAverage / 2.0;
                    // 소수 첫 번째 자리까지 포맷팅
                    average = String.format("%.1f", convertedAverage);
                }
                log.info("getMovieDTOAsMovieList >> movieNm: {}, primaryReleaseYear:{},  average: {}", movieNm, primaryReleaseYear, average);
            } else {
                log.warn("getMovieDTOAsMovieList >> response에 영화 결과가 없거나 'results' 배열이 비어 있습니다.");
            }


            //DB에 존재하는 영화이면 그대로 보여줌
            Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
            if (movieObj.isPresent()) {
                //미공개 영화는 보여주지 않
                if(movieObj.get().getStatus() == 0)
                    continue;

                Movie movie = movieObj.get();
                Long movieId = movie.getId();
                String posterURL = movie.getPosterURL();
//                List<MovieGenre> genreList = movieGenreRepository.findByMovieIdAndStatus(movieId); //장르 제외
                MovieDTO movieDTO = MovieDTO.builder()
                        .movieNm(movieNm)
                        .openDt(openDt)
                        .posterURL(posterURL)
//                        .genreList(genreList) //장르는 제외
                        .average(average)
                        .build();
                movieDTOList.add(movieDTO);
            } else { //DB에 없으면 포스터, 장르등을 추가로 API요청해서 보여줌
                //포스터 가져오기
                String posterURL = tmdbRequestService.getMoviePostURLFAST(movieCd, movieNm, movieNmEn);
                if(posterURL == null){
                    posterURL = "/image/noImage.png"; //이미지없음으로 보여줄거임
                }

                //장르 리스트를 가져오기위해 : 너무 느려서 뺀다.
//                //영화의 상세 정보를 받기 새로운 KOBIS API 요청을 날림
//                HttpResponse<String> movieDetailResponse = kobisRequestService.sendMovieDetailRequest(movieCd);
//                String movieDetailResponseBody = movieDetailResponse.body();
//                ObjectMapper movieDetailObjectMapper = new ObjectMapper();
//                JsonNode movieDetailJsonNode = movieDetailObjectMapper.readTree(movieDetailResponseBody);
//                JsonNode movieDetailNode = movieDetailJsonNode.path("movieInfoResult").path("movieInfo");
//                JsonNode genresListNode = movieDetailNode.path("genres");
//                List<MovieGenre> genreList = new ArrayList<>();
//                for (int j = 0; j < genresListNode.size(); j++) {
//                    MovieGenre movieGenre = new MovieGenre();
//                    movieGenre.setGenreNm(genresListNode.get(j).path("genreNm").asText());
//                    genreList.add(movieGenre);
//                }

                //나중에 영화검색결과에 필요한 엔티티가있으면 추가할것...

                MovieDTO movieDTO = MovieDTO.builder()
                        .movieNm(movieNm)
                        .openDt(openDt)
                        .posterURL(posterURL)
//                        .genreList(genreList) //너무 느려서 뺌
                        .average(average)
                        .build();
                movieDTOList.add(movieDTO);
            }
        }

        return movieDTOList;
    }
    
    //TMDB API의 인물 검색 목록으로 MovieDTO를 만드는 함수
    public List<PeopleDTO> getPeopleDTOAsSearchPeople(String keyword) throws Exception{
        HttpResponse<String> response = tmdbRequestService.sendSearchPeople(keyword, 1L); //첫번째 페이지 가져옴
        if(response == null){
            log.error("getPeopleDTOAsSearchPeople response 요청 실패!!!!");
            //에러 처리
            return null;
        }
        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long totalPage = jsonNode.path("total_pages").asLong();
        if(totalPage > 20L) totalPage = 20L; //인물 페이지 최대 20개로 제한

        //1. 유효한 인물 정보를 찾는다.
        List<PeopleDTO> peopleDTOList = new ArrayList<>();
        for(Long i = 0L ; i < totalPage ; i++){
            response = tmdbRequestService.sendSearchPeople(keyword, i+1);
            if(response == null){
                log.error("getPeopleDTOAsSearchPeople response 요청 실패!!!!");
                //에러 처리
                continue;
            }
            responseBody = response.body();
            objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(responseBody);
            //현재 페이지의 인물 리스트
            JsonNode peopleList = jsonNode.path("results");
            for(int j = 0 ; j < peopleList.size(); j++){
                JsonNode peopleNode = peopleList.get(j); //현재 인물
                log.warn(peopleNode.asText());
                log.error("{}index 파싱 시작", i);
                JsonNode profilePathNode = peopleNode.path("profile_path");
                if (profilePathNode.isNull() || profilePathNode.asText().isEmpty()){ //프로필 사진 없으면 제외
                    log.error("프로필 사진 없으면 제외");
                    continue;
                }
                JsonNode knownForNode = peopleNode.path("known_for");
                if (!knownForNode.isArray() || knownForNode.size() == 0){ //출연작 없으면 제외
                    log.error("출연작 없으면 제외");
                    continue;
                }

                String peopleNm = peopleNode.path("original_name").asText();
                Long peopleId = peopleNode.path("id").asLong();
                String profilePath = "https://image.tmdb.org/t/p/w500" + peopleNode.path("profile_path").asText();
                String moviePartNm = peopleNode.path("known_for_department").asText();
                if ("Acting".equals(moviePartNm)) {
                    moviePartNm = "배우";
                } else if ("Directing".equals(moviePartNm)) {
                    moviePartNm = "감독";
                } else if ("Writing".equals(moviePartNm)) {
                    moviePartNm = "작가";
                } else {
                    //그 외의 직업은 검색결과에서 제외
                    log.error("그 외의 직업은 검색결과에서 제외");
                    continue;
                }

                log.warn("{}index => peopleDTO 잘 가져옴", i);
                PeopleDTO peopleDTO = PeopleDTO.builder()
                        .peopleNm(peopleNm)
                        .peopleId(peopleId)
                        .profilePath(profilePath)
                        .moviePartNm(moviePartNm)
                        .build();
                log.info("인물 추가 >> peopleNm: {}, peopleId: {}, profilePath: {}, moviePartNm: {}", peopleNm, peopleId, profilePath, moviePartNm);
                peopleDTOList.add(peopleDTO);
            }
        }
        return peopleDTOList;
    }

    //프로필을 누르면 해당 인물의 출연 영화를 전부 보여주는 함수 : 내부적으로 getMovieDTOAsMovieList을 재사용
    public List<MovieDTO> getMovieDTOAsSearchPeopleId(Long peopleId, int limit) throws Exception{
        HttpResponse<String> response = tmdbRequestService.sendSearchMovieListAsPeopleId(peopleId, 1L); //첫번째 페이지 가져옴
        if(response == null){
            log.error("getMovieDTOAsSearchPeopleId response 요청 실패!!!!");
            //에러 처리
            return null;
        }
        String responseBody = response.body();
//        log.info("getMovieDTOAsSearchPeopleId responseBody: {}", responseBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long totalPage = jsonNode.path("total_pages").asLong();
        if(totalPage > 10L) totalPage = 10L; //영화 페이지 최대 10개로 제한
//        log.info("getMovieDTOAsSearchPeopleId totalPage: {}", totalPage);
        //???
        List<MovieDTO> movieDTOList = new ArrayList<>();
        for(Long i = 0L ; i < totalPage ; i++){
            response = tmdbRequestService.sendSearchMovieListAsPeopleId(peopleId, i + 1); //1페이지부터 다시 요청
            if(response == null){
                log.error("getMovieDTOAsSearchPeopleId response 요청 실패!!!!");
                //에러 처리
                continue;
            }
            responseBody = response.body();
//            log.info("getMovieDTOAsSearchPeopleId index:{}  responseBody: {}", i, responseBody);
            objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(responseBody);
            //???
            JsonNode movieList = jsonNode.path("results");
//            log.info("getMovieDTOAsSearchPeopleId movieList.size(): {}", movieList.size());
            for(int j = 0 ; j < movieList.size(); j++) {
                JsonNode movieNode = movieList.get(j); //현재 영화
                JsonNode movieNmNode = movieNode.path("original_title");
                JsonNode openStartDtNode = movieNode.path("release_date");
                if (movieNmNode.isNull() || movieNmNode.asText().isEmpty()) { //영화 제목이 없으면 제외
//                    log.error("getMovieDTOAsSearchPeopleId : 영화제목이 없음 >> {}", movieNmNode.asText());
                    continue;
                }
                if (openStartDtNode.isNull() || openStartDtNode.asText().isEmpty()) { //영화 제작년도가 없으면 제외
//                    log.error("getMovieDTOAsSearchPeopleId : 개봉년도가 없음 >> {}", openStartDtNode.asText());
                    continue;
                }

                //영화에 필요한 정보를 가져옴
                String movieNm = movieNmNode.asText();
                String openStartDt = openStartDtNode.asText().substring(0, 4);
                String openEndDt = openStartDt; //검색 기간을 해당 년도만으로 제한
                log.info("getMovieDTOAsSearchPeopleId movieNm:{}", movieNm);
                response = kobisRequestService.sendMovieListRequestByMovieNm(movieNm, openStartDt, openEndDt);
                if(response == null){
                    log.error("getMovieDTOAsSearchPeopleId kobisRequestService.sendMovieListRequestByMovieNm 요청 실패!!!!");
                    //에러 처리
                    continue;
                }

//                log.info("getMovieDTOAsSearchPeopleId i-index:{} j-index:{}  영화제목,조회기간으로 영화 검색 결과 responseBody: {}", i, j, response);

                //DTO생성
                int remainCnt = limit - movieDTOList.size();
                List<MovieDTO> findMovieDTO = getMovieDTOAsMovieList(response, remainCnt); //최대 갯수 제한해서 검색

                if(findMovieDTO == null){
                    //포스터 가져오기
                    JsonNode posterPathNode = movieNode.path("poster_path");
                    String posterURL = "https://image.tmdb.org/t/p/w500" + posterPathNode.asText();
                    //평점 가져오기 (소숫점 1자리까지만)
                    JsonNode averageNode = movieNode.path("vote_average");
                    String average = averageNode.asText().split(".")[0] + averageNode.asText().split(".")[1].substring(0, 1);
                    MovieDTO movieDTO = MovieDTO.builder()
                            .movieNm(movieNm)
                            .openDt(openStartDt)
                            .posterURL(posterURL)
                            .average(average) //평점 기록
                            .build();
                    findMovieDTO = new ArrayList<>();
                    findMovieDTO.add(movieDTO);
                }
                //결과에 추가
                movieDTOList.addAll(findMovieDTO);

                //최대 제한에 다다르면 함수를 종료
                if(movieDTOList.size() >= limit) return movieDTOList;
            }
        }

        return movieDTOList;
    }


    //search : 영화 이름으로 영화리스트를 검색
    public List<MovieDTO> searchMovieAsMovieName(String keyword, int limit) throws Exception{
        HttpResponse<String> response = kobisRequestService.sendMovieListRequestByMovieNm(keyword);
        if(response == null){
            log.error("searchMovieAsMovieName response 요청 실패!!!!");
            //에러 처리
            return null;
        }
        log.info("영화이름으로 검색 결과 response body = > {}", response.body());
        List<MovieDTO> movieList = getMovieDTOAsMovieList(response, limit); //최대 갯수 제한

        return movieList;
    }

    //search : 인물 이름으로 인물들의 프로필 리스트 검색
    public List<PeopleDTO> searchProfileAsPeopleName(String keyword) throws Exception{
        List<PeopleDTO> peopleDTOList = getPeopleDTOAsSearchPeople(keyword);
        return peopleDTOList;
    }

    //프로필을 클릭했을때 해당 사람의 출연 영화를 전부 보여줌
    public List<MovieDTO> searchMovieAsPeopleId(Long peopleId, int limit) throws Exception{
        List<MovieDTO> movieDTOList = getMovieDTOAsSearchPeopleId(peopleId, limit); //검색 결과 제한
        return movieDTOList;
    }






}
