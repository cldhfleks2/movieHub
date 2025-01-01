package com.cldhfleks2.moviehub.report.movie;

import com.cldhfleks2.moviehub.member.Member;
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
@SQLDelete(sql = "UPDATE movie_review SET status = 0 WHERE id = ?")
@ToString
public class MovieReport {
    // 신고 사유별 필드
    private Boolean POSTER;   // 주요 스포일러 포함
    private Boolean MOVIENAME;     // 잘못된 정보
    private Boolean MOVIEPEOPLE; // 무관한 내용/광고성
    private Boolean HARMFUL;   // 유해하거나 불건전한 내용
    private Boolean HATE;      // 혐오 발언

    @Column(length = 500)
    private String reportDetail; // 추가 설명 (선택사항)

    @ToString.Exclude
    @ManyToOne
    private Member member; //신고한 유저

    @ToString.Exclude
    @ManyToOne
    private Movie movie; //신고할 영화

    //아래는 기본 필드들
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;
    @UpdateTimestamp
    private LocalDateTime updateDate;
    private int status = 1; //존재 여부 (1:보임 0:삭제)
}
