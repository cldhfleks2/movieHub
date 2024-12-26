package com.cldhfleks2.moviehub.movie;

import com.cldhfleks2.moviehub.movie.actor.MovieActor;
import com.cldhfleks2.moviehub.movie.audit.MovieAudit;
import com.cldhfleks2.moviehub.movie.director.MovieDirector;
import com.cldhfleks2.moviehub.movie.genre.MovieGenre;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MovieDTO {
    //메인 페이지 에서 사용
    private String movieCd; // 영화 고유 코드  예) "20200142"
    private String movieNm; // 영화 이름
    private String movieNmEn;
    private List<MovieGenre> genreList; // 리스트!!       장르명   예) "드라마"
    private List<MovieAudit> auditList; // 리스트!!  관람 등급  예) "12세이상관람가"
    private String showTm; // 영화 상영 시간     예) "106" 단위 분
    private String openDt; // 개봉일     예) "2024-12-04(일별박스오피스)" "20241204(영화상세정보)"
    private String audiAcc; // 누적 관객 수       예) "2191719"
    private String audiCnt; // 해당 일 관객 수 예) "1438"
    private String posterURL;

    //영화 상세 페이지에서 사용
    private List<MovieDirector> directorList;
    private List<MovieActor> actorList;

    //검색에서 사용
    private String rating;

}

