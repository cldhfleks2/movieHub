package com.cldhfleks2.moviehub.entity;

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
//JPA의 delete메소드 실행시 soft삭제를위해 status=0, update_date를 현재시간으로 설정
//오류나면 테이블이름이 제대로 작성된건지 확인하자
@SQLDelete(sql = "UPDATE movie_daily_stat SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
public class MovieDailyStat {
    //일별박스오피스
    private String day; // 일별박스오피스의 showRange에 일치하는 날짜. 예) "20241220"
    private String audiCnt; // 누적 매출액 :
    private String scrnCnt; // 스크린 수  예) "1438" 잘안쓸거같긴해
    private String showCnt; // 상영 횟수  예) "5173" 잘안쓸거같긴해


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
