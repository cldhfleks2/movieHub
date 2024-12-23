package com.cldhfleks2.moviehub.movie;

import com.cldhfleks2.moviehub.KOBISRequestService;
import com.cldhfleks2.moviehub.TMDBRequestService;
import com.cldhfleks2.moviehub.config.SeleniumWebDriverConfig;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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

    //영화목록response을 파싱해서 나온 JsonNode로 MovieDTO를 생성
    public List<MovieDTO> getMovieDTOAsJsonNode(JsonNode jsonNodeList) throws Exception {
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

    //MovieId 로 MovieDTO를 생성
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

        MovieDTO.MovieDTOBuilder builder = MovieDTO.builder();
        builder.movieCd(movie.getMovieCd());
        builder.movieNm(movie.getMovieNm());
        builder.movieNmEn(movie.getMovieNmEn());
        builder.genreList(genreList);
        builder.auditList(auditList);
        builder.showTm(movie.getShowTm());
        builder.openDt(movie.getOpenDt());
        builder.audiAcc(movie.getAudiAcc());
        builder.audiCnt(movieDailyStat.getAudiCnt());
        builder.posterURL(movie.getPosterURL());
        builder.directorList(directorList);
        builder.actorList(actorList);

        return builder.build();
    }

    //ReturnEntitysDTO(영화관련묶음엔티티) => MovieDTO
    public MovieDTO convertToMovieDTO(ReturnEntitysDTO returnEntitysDTO) {
        MovieDTO.MovieDTOBuilder builder = MovieDTO.builder();
        builder.movieCd(returnEntitysDTO.getMovie().getMovieCd());
        builder.movieNm(returnEntitysDTO.getMovie().getMovieNm());
        builder.movieNmEn(returnEntitysDTO.getMovie().getMovieNmEn());
        builder.genreList(returnEntitysDTO.getMovieGenreList());
        builder.auditList(returnEntitysDTO.getMovieAuditList());
        builder.showTm(returnEntitysDTO.getMovie().getShowTm());
        builder.openDt(returnEntitysDTO.getMovie().getOpenDt());
        builder.audiAcc(returnEntitysDTO.getMovie().getAudiAcc());
        builder.audiCnt(returnEntitysDTO.getMovieDailyStat().getAudiCnt());
        builder.posterURL(returnEntitysDTO.getMovie().getPosterURL());

        return builder.build();
    }

    //영화 상세 정보을 DB에 저장 : movieInfo가 정상적으로 있는지 확인은 상위 함수에서 처리했을거임. 저장만 하는함수
    public ReturnEntitysDTO saveEntityAsMovieDetailResponse(JsonNode movieInfo, String currentDay) throws Exception {
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
        //포스터 가져오기
        String posterURL = tmdbRequestService.getMoviePosterURL(movieNm); //새로운 기능. 영화이름으로 검색
        if(posterURL == null){
            movie.setPosterURL("null");
            movie.setStatus(0); //미공개 영화로 저장
            movieRepository.save(movie);
            return null; //다른 엔티티를 저장하지 않으며 null값 리턴
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

        ReturnEntitysDTO.ReturnEntitysDTOBuilder builder = ReturnEntitysDTO.builder();
        builder.movie(movie);
        builder.movieDailyStat(movieDailyStat);
        builder.movieDirectorList(movieDirectorList);
        builder.movieActorList(movieActorList);
        builder.movieNationList(movieNationList);
        builder.movieGenreList(movieGenreList);
        builder.movieCompanyList(movieCompanyList);
        builder.movieAuditList(movieAuditList);
        ReturnEntitysDTO returnEntitysDTO = builder.build();

        return returnEntitysDTO;
    }

    //박스오피스 목록을 DB에 저장 : 실제 JsonNode를 탐색하여 저장
    //saveTodayBoxOfficeOnDB, saveWeeklyBoxOfficeOnDB 에서 사용
    public void saveEntityAsBoxOfficeResponse(JsonNode boxOfficeList, String currentDay) throws Exception {
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

                //movieDailyStat 정보 업데이트
                MovieDailyStat movieDailyStat = movieDailyStatObj.get();
                movieDailyStat.setSalesAmt(salesAmt); // 일일 기록
                movieDailyStat.setAudiCnt(audiCnt); // 일일 기록
                movieDailyStat.setScrnCnt(scrnCnt); // 스크린 수
                movieDailyStat.setShowCnt(showCnt); // 상영 횟수
                movieDailyStatRepository.save(movieDailyStat); //수정
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
        saveEntityAsBoxOfficeResponse(weeklyBoxOfficeList, currentDay);
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
        saveEntityAsBoxOfficeResponse(todayBoxOfficeList, currentDay);
    }


}
