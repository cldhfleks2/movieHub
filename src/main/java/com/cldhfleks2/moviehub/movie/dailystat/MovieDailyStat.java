package com.cldhfleks2.moviehub.movie.dailystat;

import com.cldhfleks2.moviehub.movie.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE movie_daily_stat SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
//유일하게 하루마다 값이 바뀌어야하는 엔티티
public class MovieDailyStat {
    //일별박스오피스
    private String day; // 일자(showRange날짜) 예) "20241220"
    private String salesAmt; // 해당일의 매출액 예) "1010084470"
    private String audiCnt; // 해당 일 관객 수 예) "1438"
    private String scrnCnt; // 스크린 수      예) "1176" 잘안쓸거같긴해
    private String showCnt; // 상영 횟수      예) "3698" 잘안쓸거같긴해


    //아래는 기본 필드들
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;
    @UpdateTimestamp
    private LocalDateTime updateDate;
    //리뷰 존재 여부 (1:보임 0:삭제)
    private int status = 1;
}
