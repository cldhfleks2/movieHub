package com.cldhfleks2.moviehub.movie.audit;

import com.cldhfleks2.moviehub.movie.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class MovieAudit {
    //영화상세정보
    private String watchGradeNm; // 관람 등급  예) "12세이상관람가"

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
