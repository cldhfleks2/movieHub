package com.cldhfleks2.moviehub.movie.director;

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
//JPA의 delete메소드 실행시 soft삭제를위해 status=0, update_date를 현재시간으로 설정
@SQLDelete(sql = "UPDATE movie_director SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
public class MovieDirector {
    //영화상세정보
    private String peopleNm; // 감독 이름 한글    예) "곽경택"
    private String peopleNmEn; // 감독 이름 영문  예) "KWAK Kyung-taek"

    //아래는 기본 필드들
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "movie_id") 지정 안해도 자동으로 movie_id 생성
    private Movie movie;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;
    @UpdateTimestamp
    private LocalDateTime updateDate;
    //리뷰 존재 여부 (1:보임 0:삭제)
    private int status = 1;
}
