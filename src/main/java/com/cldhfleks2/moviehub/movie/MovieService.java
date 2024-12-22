package com.cldhfleks2.moviehub.movie;

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
    private final SeleniumWebDriverConfig seleniumWebDriverConfig;


    @Value("${kobis.key}")
    private String kobiskey;

    //KOBIS URL로 요청을 보내고 응답을 리턴해주는 함수
    public HttpResponse<String> sendRequest(String URL) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    //재귀적으로 JsonNode에 있는 다차원 구조의 키를 1차원 구조의 Map으로 옮김.
    //키가 겹친다면 사고가 나겠지만 그런경우는 드믐
    private void translateJsonNodeRecursively(JsonNode jsonNode, Map<String, String> resultMap, String parentKey) {
        if (jsonNode.isObject()) {
            // 객체일 경우, 각 필드를 키로 하여 재귀 호출
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = parentKey.isEmpty() ? field.getKey() : parentKey + "." + field.getKey();
                translateJsonNodeRecursively(field.getValue(), resultMap, key);
            }
        } else if (jsonNode.isArray()) {
            // 배열일 경우, 각 요소의 인덱스를 키로 하여 재귀 호출
            for (int i = 0; i < jsonNode.size(); i++) {
                String key = parentKey + "[" + i + "]";
                translateJsonNodeRecursively(jsonNode.get(i), resultMap, key);
            }
        } else {
            // 값일 경우, 키-값 쌍을 Map에 추가
            resultMap.put(parentKey, jsonNode.asText());
        }
    }

    public Map<String, String> translateJsonToMap(JsonNode jsonNode) {
        Map<String, String> resultMap = new HashMap<>();
        translateJsonNodeRecursively(jsonNode, resultMap, "");
        System.out.println("JsonNode -> Map<Str, Str> 결과 = " + resultMap);
        return resultMap;
    }

    //전체일일박스오피스 목록의 응답인 response를 파싱해서 Movie객체등 여러 엔티티를 DB에 추가하는 함수
    //앱 실행시 최초 1회 진행
    public void saveTotalTodayBoxOffice() throws Exception {
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
        if (!currentDay.equals(day)) {
            //excuteTask에있는 response를 새로 요청해서 받아야함
            excuteTask.getTotalTodayBoxOfficeResponse();
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
            //안쓰는건 주석처리 해둠 : rank, rnum, rankInten, rankOldAndNew, salesChange, salesShare, salesInten, audiChange

            //movieCd Movie가 DB에 존재 하는지 확인
            Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
            //1. Movie객체가 존재할때
            if (movieObj.isPresent()) {
                //미공개 영화는 보여주지 않는다.
                //예로들면 포스터 이미지를 가져오는데 실패하면 movie객체를 status=0으로 DB에 저장하게 해놧음
                if(movieObj.get().getStatus() == 0)
                    continue;
                Movie movie = movieObj.get();
                Long movie_id = movie.getId();
                //Movie가 이미 존재 하면 actor,audit,company,director,genre,nation은 이미 추가되었을것이고
                //dailyStat도 추가 되었겠지만 이것은 매일 변하는 값들을 가짐.
                //dailyStat를 매일 딱 한번 변경 해줘야함.
                //movie_id와 currentDay로 검색
                Optional<MovieDailyStat> movieDailyStatObj = movieDailyStatRepository.findByDayAndMovieIdAndStatus(currentDay, movie_id);
                //2. 현재 날짜인 MovieDailyStat이 존재 할때
                if (movieDailyStatObj.isPresent()) {
                    //continue;
                } else {
                    //2. 현재 날짜인 MovieDailyStat이 없을때
                    //MovieDailyStat를 movie_id로 찾아서 모든 값들을 변경
                    List<MovieDailyStat> movieDailyStatList = movieDailyStatRepository.findByMovieIdAndStatus(movie_id);
                    //3. movie_id와 일치하는 MovieDailyStat이 존재 할때
                    if (!movieDailyStatList.isEmpty()) {
                        MovieDailyStat movieDailyStat = movieDailyStatList.get(movieDailyStatList.size() - 1); //가장 최근것
                        movieDailyStat.setDay(currentDay); //현재 날짜로 수정
                        movieDailyStatRepository.save(movieDailyStat);
                    } else {
                        //3. movie_id와 일치하는 MovieDailyStat이 없을때
                        //Movie는 존재하는데
                        //현재날짜에대한 MovieDailyStat이없고
                        //movie_id로도 조회가 안되면
                        //Movie를 저장할때 같이 저장안된건데.. 일단 추가해주자
                        //이런 경우는 거의 없다.
                    }
                }

            } else {
                //1. Movie객체가 없을때
                //새로 여러 entity들을 DB에 저장 해야함
                Movie movie = new Movie(); //Movie 엔티티
                movie.setMovieCd(movieCd);
                movie.setMovieNm(movieNm);
                movie.setOpenDt(openDt);
                movie.setSalesAcc(salesAcc);
                movie.setAudiAcc(audiAcc);
                //여기에 영화 포스터를 가져오는 셀레니움 로직 작성
                String getPosterURL = seleniumWebDriverConfig.getMoviePosterURL(movieCd);
                // 영화 포스터가 없으면 해당 영화를 미공개 영화로 저장
                if (getPosterURL == null || !getPosterURL.contains("source=")) {
                    movie.setPosterURL("null");
                    movie.setStatus(0); //미공개 영화로 저장
                    movieRepository.save(movie);
                    continue; //다른 엔티티를 저장하지 않음.
                }
                String posterURL = getPosterURL.split("source=")[1];
                movie.setPosterURL(posterURL);

                //영화의 상세 정보를 받기 새로운 KOBIS API 요청을 날려야함
                //KOBIS API : 영화상세정보 보는 요청
                String movieDetailURL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json"
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
                    movieNation.setMovie(movie);
                    movieNationRepository.save(movieNation);
                }
                JsonNode genresList = movieDetail.path("genres");
                for (int j = 0; j < genresList.size(); j++) {
                    MovieGenre movieGenre = new MovieGenre();
                    movieGenre.setGenreNm(genresList.get(j).path("genreNm").asText());
                    movieGenre.setMovie(movie);
                    movieGenreRepository.save(movieGenre);
                }
                JsonNode directorsList = movieDetail.path("directors");
                for (int j = 0; j < directorsList.size(); j++) {
                    MovieDirector movieDirector = new MovieDirector();
                    movieDirector.setPeopleNm(directorsList.get(j).path("peopleNm").asText());
                    movieDirector.setPeopleNmEn(directorsList.get(j).path("peopleNmEn").asText());
                    movieDirector.setMovie(movie);
                    movieDirectorRepository.save(movieDirector);
                }
                JsonNode actorsList = movieDetail.path("actors");
                for (int j = 0; j < actorsList.size(); j++) {
                    MovieActor movieActor = new MovieActor();
                    movieActor.setPeopleNm(actorsList.get(j).path("peopleNm").asText());
                    movieActor.setPeopleNmEn(actorsList.get(j).path("peopleNmEn").asText());
                    movieActor.setMovie(movie);
                    movieActorRepository.save(movieActor);
                }
                JsonNode companysList = movieDetail.path("companys");
                for (int j = 0; j < companysList.size(); j++) {
                    MovieCompany movieCompany = new MovieCompany();
                    movieCompany.setCompanyCd(companysList.get(j).path("").asText());
                    movieCompany.setCompanyNm(companysList.get(j).path("companyNm").asText());
                    movieCompany.setCompanyNmEn(companysList.get(j).path("companyNmEn").asText());
                    movieCompany.setCompanyPartNm(companysList.get(j).path("companyPartNm").asText());
                    movieCompany.setMovie(movie);
                    movieCompanyRepository.save(movieCompany);
                }
                JsonNode auditsList = movieDetail.path("audits");
                for (int j = 0; j < auditsList.size(); j++) {
                    MovieAudit movieAudit = new MovieAudit();
                    movieAudit.setWatchGradeNm(auditsList.get(j).path("watchGradeNm").asText());
                    movieAudit.setMovie(movie);
                    movieAuditRepository.save(movieAudit);
                }
            }
        }
    }
    
    //검색 페이지나 상세페이지(아직 DB에없는 movieCd로 상세페이지 접근시) 에서 DB에 영화를 저장하는 로직
    //자주 쓸
    public void saveMovie(){

    }

}
