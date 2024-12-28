package com.cldhfleks2.moviehub.report;

import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.review.MovieReview;
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
@SQLDelete(sql = "UPDATE movie_review_report SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
public class MovieReviewReport {
    // 신고 사유별 필드
    private Boolean SPOILER;   // 주요 스포일러 포함
    private Boolean WRONG;     // 잘못된 정보
    private Boolean UNRELATED; // 무관한 내용/광고성
    private Boolean HARMFUL;   // 유해하거나 불건전한 내용
    private Boolean HATE;      // 혐오 발언
    private Boolean COPYRIGHT; // 저작권 침해
    private Boolean SPAM;      // 개인정보 노출

    @Column(length = 500)
    private String reportDetail; // 추가 설명 (선택사항)

    @ToString.Exclude
    @ManyToOne
    private Member member; //신고한 유저

    @ToString.Exclude
    @ManyToOne
    private MovieReview movieReview; //신고할 리뷰

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
