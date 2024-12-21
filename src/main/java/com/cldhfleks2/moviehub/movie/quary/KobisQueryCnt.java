package com.cldhfleks2.moviehub.movie.quary;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class KobisQueryCnt {
    private String day; //날짜
    //해당 날짜에 호출한 횟수들 카운트
    private Long totalTodayBoxOffice=0L; 
    private Long totalWeeklyBoxOffice=0L;
    private Long koreaWeeklyBoxOffice=0L;
    private Long foreignWeeklyBoxOffice=0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //리뷰 존재 여부 (1:보임 0:삭제)
    private int status = 1;
}
