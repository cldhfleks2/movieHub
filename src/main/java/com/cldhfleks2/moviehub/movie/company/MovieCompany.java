package com.cldhfleks2.moviehub.movie.company;

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
public class MovieCompany {
    //영화상세정보
    private String companyCd; //회사 고유 코드     예) "20229461"
    private String companyNm; //회사 이름 한글     예) "(주)바이포엠스튜디오"
    private String companyNmEn; //회사 이름 영문   예) "BY4MSTUDIO"
    private String companyPartNm; //회사의 역할    예) "제공"

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
