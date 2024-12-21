package com.cldhfleks2.moviehub.movie;

import com.cldhfleks2.moviehub.config.ExcuteTask;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieNationRepository movieNationRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieCompanyRepository movieCompanyRepository;
    private final MovieDailyStatRepository movieDailyStatRepository;
    private final MovieDirectorRepository movieDirectorRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieAuditRepository movieAuditRepository;
    private final MovieRepository movieRepository;
    private final ExcuteTask excuteTask;


    @Value("/kobis.key")
    private String kobiskey;

    //KOBIS URL로 요청을 보내고 응답을 리턴해주는 함수
    public HttpResponse<String> sendRequest(String URL) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    //오늘의박스오피스목록 응답데이터를 파싱하여
    //DB에 없는 Movie객체를 추가해주고
    //관련된 엔티티도 업데이트
    public void saveTodayBoxOffice() throws Exception {
        //날짜 가져오기
        //KOBIS에서 하루이전 통계만 잡히므로 하루 이전 날짜로 계산
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        HttpResponse<String> totalTodayBoxOfficeResponse = excuteTask.getTotalTodayBoxOfficeResponseBody();
        String totalTodayBoxOfficeResponseBody = totalTodayBoxOfficeResponse.body();

        //JSON파싱 진행
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(totalTodayBoxOfficeResponseBody);
        JsonNode totalTodayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");
        //날짜표기중 ~ 기준으로 앞의 날짜만 가져옴    예) "20241220"
        String day = jsonNode.path("boxOfficeResult").path("showRange").asText().split("~")[0];

        //현재 날짜와 response에있는 날짜가 다르면
        if(currentDay != day){
            //excuteTask에있는 response를 새로 요청해서 받아야함
            excuteTask.getTotalTodayBoxOfficeTask();
            //새로 받은 response로 다시 JSON파싱 진행
            totalTodayBoxOfficeResponse = excuteTask.getTotalTodayBoxOfficeResponseBody();
            totalTodayBoxOfficeResponseBody = totalTodayBoxOfficeResponse.body();
            jsonNode = objectMapper.readTree(totalTodayBoxOfficeResponseBody);
            totalTodayBoxOfficeList = jsonNode.path("boxOfficeResult").path("dailyBoxOfficeList");
        }

        // 전체 영화 목록에 대해서 DB에 저장하는 로직
        for (int i = 0; i < totalTodayBoxOfficeList.size() && i < 10; i++) {
            //현재 영화에 대한 처리
            JsonNode movieJsonNode = totalTodayBoxOfficeList.get(i);

            String movieCd = movieJsonNode.path("movieCd").asText();
            String movieNm = movieJsonNode.path("movieNm").asText();
            String openDt = movieJsonNode.path("openDt").asText();
            String salesAcc = movieJsonNode.path("salesAcc").asText();
            String audiAcc = movieJsonNode.path("audiAcc").asText();
            String salesAmt = movieJsonNode.path("salesAmt").asText();
            String audiCnt = movieJsonNode.path("audiCnt").asText();
            String scrnCnt = movieJsonNode.path("scrnCnt").asText();
            String showCnt = movieJsonNode.path("showCnt").asText();
            //안쓰는건 주석처리 해둠
//            String rank = movieJsonNode.path("rank").asText(); //순위를 어딘가는 써야하는데 아직 안씀
//            JsonNode rnum = movie.path("rnum");
//            JsonNode rankInten = movie.path("rankInten"); //전일 대비 순위 얼마나 바뀌었는가
//            JsonNode rankOldAndNew = movie.path("rankOldAndNew"); //기존:OLD 새로:NEW
//            JsonNode salesChange = movie.path("salesChange"); //전일 대비 영화 매출 증감 소숫점
//            JsonNode salesShare = movie.path("salesShare"); //오늘 전체 영화 총 매출중에 몇퍼센트 부담하는가 소숫점
//            JsonNode salesInten = movie.path("salesInten"); //전일 대비 영화 매출 증감 금액
//            JsonNode audiInten = movie.path("audiInten"); //전일 대비 관객수 증감 명수
//            JsonNode audiChange = movie.path("audiChange"); //전일 대비 관객수 증감 소숫점

            //movieCd Movie가 DB에 존재 하는지 확인
            Optional<Movie> movieObj =  movieRepository.findByMovieCd(movieCd);
            //1. Movie객체가 존재할때
            if(movieObj.isPresent()) {
                //Movie가 이미 존재 하면 actor,audit,company,director,genre,nation은 이미 추가되었을것이고
                //dailyStat도 추가 되었겠지만 이것은 매일 변하는 값들을 가짐.
                //dailyStat를 매일 딱 한번 변경 해줘야함.
                Optional<MovieDailyStat> movieDailyStatObj = movieDailyStatRepository.findByDay(currentDay);
                //2. 현재 날짜인 MovieDailyStat이 존재 할때
                if(movieDailyStatObj.isPresent()) {
                    //continue;
                }else{
                    //2. 현재 날짜인 MovieDailyStat이 없을때
                    //MovieDailyStat를 movie_id로 찾아서 모든 값들을 변경
                    Movie movie = movieObj.get();
                    movieDailyStatObj = movieDailyStatRepository.findByMovieId(movie.getId());
                    //3. movie_id와 일치하는 MovieDailyStat이 존재 할때
                    if(movieDailyStatObj.isPresent()) {
                        MovieDailyStat movieDailyStat = movieDailyStatObj.get();
                        movieDailyStat.setDay(currentDay); //현재 날짜로 수정
                        movieDailyStatRepository.save(movieDailyStat);
                    }else{
                        //3. movie_id와 일치하는 MovieDailyStat이 없을때
                        //Movie는 존재하는데
                        //현재날짜에대한 MovieDailyStat이없고
                        //movie_id로도 조회가 안되면
                        //Movie를 저장할때 같이 저장안된건데.. 일단 추가해주자
                        //이런 경우는 거의 없다.
                        MovieDailyStat movieDailyStat = new MovieDailyStat();
                        movieDailyStat.setDay(currentDay); //"20241220"
                        movieDailyStat.setSalesAmt(salesAmt);
                        movieDailyStat.setAudiCnt(audiCnt);
                        movieDailyStat.setScrnCnt(scrnCnt);
                        movieDailyStat.setShowCnt(showCnt);
                        movieDailyStatRepository.save(movieDailyStat);
                    }
                }

            }else{
                //1. Movie객체가 없을때
                //새로 여러 entity들을 DB에 저장 해야함
                Movie movie = new Movie(); //Movie 엔티티
                movie.setMovieCd(movieCd);
                movie.setMovieNm(movieNm);
                movie.setOpenDt(openDt);
                movie.setSalesAcc(salesAcc);
                movie.setAudiAcc(audiAcc);
                movieRepository.save(movie); //가장 먼저 저장해줘야 다른 엔티티들이 외래키로 사용가능

                MovieDailyStat movieDailyStat = new MovieDailyStat(); //MovieDailyStat 엔티티
                movieDailyStat.setDay(currentDay); //"20241220"
                movieDailyStat.setSalesAmt(salesAmt);
                movieDailyStat.setAudiCnt(audiCnt);
                movieDailyStat.setScrnCnt(scrnCnt);
                movieDailyStat.setShowCnt(showCnt);
                movieDailyStatRepository.save(movieDailyStat);

                //영화의 상세 정보를 받기 새로운 KOBIS API 요청을 날려야함
                //KOBIS API : 전체 일일박스오피스 10개 가져오는 요청 만들기
                String movieDetailURL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.xml"
                        + "?key=" + kobiskey
                        + "&movieCd=" + movieCd; //영화 코드를 보냄
                HttpResponse<String> movieDetailResponse = sendRequest(movieDetailURL);
                String movieDetailResponseBody = movieDetailResponse.body();
                ObjectMapper movieDetailObjectMapper = new ObjectMapper();
                JsonNode movieDetailJsonNode = movieDetailObjectMapper.readTree(movieDetailResponseBody);
                JsonNode movieDetail = movieDetailJsonNode.path("movieInfoResult").path("movieInfo");
                String movieNmEn = movieDetail.path("movieNmEn").asText();
                String showTm = movieDetail.path("showTm").asText();
                String prdtYear = movieDetail.path("prdtYear").asText();
                String typeNm = movieDetail.path("typeNm").asText();
                //안쓰는것들은 주석 처리함
//                String movieNmOg = movieInfo.path("movieNmOg").asText(); //entity에서 안쓰기로 했음
//                String prdtStatNm = movieInfo.path("prdtStatNm").asText();//entity에서 안쓰기로 했음
//                String showTypes = movieInfo.path("showTypes").asText();//entity에서 안쓰기로 했음(showTypeNm)
//                String staffs = movieInfo.path("staffs").asText(); //staff는 안쓰기로함
                movie.setMovieNmEn(movieNmEn);
                movie.setShowTm(showTm);
                movie.setPrdtYear(prdtYear);
                movie.setTypeNm(typeNm);

                //나머지 엔티티들 추가
                JsonNode nationsList = movieDetail.path("nations");
                for(int j = 0 ; j < nationsList.size() ; j++){
                    MovieNation movieNation = new MovieNation();
                    movieNation.setNationNm(nationsList.get(j).path("nationNm").asText());
                    movieNationRepository.save(movieNation);
                }
                JsonNode genresList = movieDetail.path("genres");
                for(int j = 0 ; j < genresList.size() ; j++){
                    MovieGenre movieGenre = new MovieGenre();
                    movieGenre.setGenreNm(genresList.get(j).path("genreNm").asText());
                    movieGenreRepository.save(movieGenre);
                }
                JsonNode directorsList = movieDetail.path("directors");
                for(int j = 0 ; j < directorsList.size() ; j++){
                    MovieDirector movieDirector = new MovieDirector();
                    movieDirector.setPeopleNm(directorsList.get(j).path("peopleNm").asText());
                    movieDirector.setPeopleNmEn(directorsList.get(j).path("peopleNmEn").asText());
                    movieDirectorRepository.save(movieDirector);
                }
                JsonNode actorsList = movieDetail.path("actors");
                for(int j = 0 ; j < actorsList.size() ; j++){
                    MovieActor movieActor = new MovieActor();
                    movieActor.setPeopleNm(actorsList.get(j).path("peopleNm").asText());
                    movieActor.setPeopleNmEn(actorsList.get(j).path("peopleNmEn").asText());
                    movieActorRepository.save(movieActor);
                }
                JsonNode companysList = movieDetail.path("companys");
                for(int j = 0 ; j < companysList.size() ; j++){
                    MovieCompany movieCompany = new MovieCompany();
                    movieCompany.setCompanyCd(companysList.get(j).path("").asText());
                    movieCompany.setCompanyNm(companysList.get(j).path("companyNm").asText());
                    movieCompany.setCompanyNmEn(companysList.get(j).path("companyNmEn").asText());
                    movieCompany.setCompanyPartNm(companysList.get(j).path("companyPartNm").asText());
                    movieCompanyRepository.save(movieCompany);
                }
                JsonNode auditsList = movieDetail.path("audits");
                for(int j = 0 ; j < auditsList.size() ; j++){
                    MovieAudit movieAudit = new MovieAudit();
                    movieAudit.setWatchGradeNm(auditsList.get(j).path("watchGradeNm").asText());
                    movieAuditRepository.save(movieAudit);
                }
            }
        }

    }


}
