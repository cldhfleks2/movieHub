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
@SQLDelete(sql = "UPDATE movie_actor SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
public class MovieActor {
    //영화상세정보
    private String peopleNm; // 배우 이름 한글    예) "주원"
    private String peopleNmEn; // 배우 이름 영문  예) "JOO Won"

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
