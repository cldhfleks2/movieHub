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
    private Boolean POSTER;   // 영화 포스터가 다릅니다.
    private Boolean MOVIENAME;     // 영화 제목이 다릅니다.
    private Boolean MOVIEPEOPLE; // 인물 정보가 잘못 되었습니다.
    private Boolean HARMFUL;   // 유해하거나 불건전한 내용이 포함되있습니다.
    private Boolean HATE;      // 불건전한 내용입니다.

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
