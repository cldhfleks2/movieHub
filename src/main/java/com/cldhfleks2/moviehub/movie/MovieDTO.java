package com.cldhfleks2.moviehub.movie;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovieDTO {
    private String movieCd; // 영화 고유 코드  예) "20200142"
    private String movieNm; // 영화 이름
    private String watchGradeNm; // 관람 등급  예) "12세이상관람가"
    private String genreNm; // 장르명   예) "드라마"
    private String showTm; // 영화 상영 시간     예) "106" 단위 분
    private String openDt; // 개봉일     예) "2024-12-04(일별박스오피스)" "20241204(영화상세정보)"
    private String audiAcc; // 누적 관객 수       예) "2191719"
    private String audiCnt; // 해당 일 관객 수 예) "1438"
}
