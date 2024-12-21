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
//오류나면 테이블이름이 제대로 작성된건지 확인하자
@SQLDelete(sql = "UPDATE movie_genre SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
public class MovieGenre {
    //영화상세정보
    private String genreNm; // 장르명   예) "드라마"

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
