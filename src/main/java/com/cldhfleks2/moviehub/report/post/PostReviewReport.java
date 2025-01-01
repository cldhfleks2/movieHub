package com.cldhfleks2.moviehub.report.post;

import com.cldhfleks2.moviehub.community.PostReview;
import com.cldhfleks2.moviehub.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE post_review_report SET status = 0 WHERE id = ?")
@ToString
@NoArgsConstructor
public class PostReviewReport {
    //신고 사유 체크 박스
    private Boolean INAPPROPRIATE;   // 부적절하거나 불건전한 내용
    private Boolean MISINFORMATION; // 허위정보 또는 사실 왜곡
    private Boolean HATE;           // 혐오 발언/차별적 내용
    private Boolean ABUSIVE;        // 욕설 또는 부적절한 언행
    private Boolean COPYRIGHT;      // 저작권 침해 의심
    private Boolean SPAM;           // 광고 또는 스팸성 게시물
    @Column(length = 500)
    private String reportDetail; // 추가 설명 (선택사항)

    @ToString.Exclude
    @ManyToOne
    private Member member; //신고한 유저
    @ToString.Exclude
    @ManyToOne
    private PostReview review; //신고할 댓글

    @Builder(builderMethodName = "create")
    public PostReviewReport(Boolean INAPPROPRIATE, Boolean MISINFORMATION, Boolean HATE, Boolean ABUSIVE,
                            Boolean COPYRIGHT, Boolean SPAM, String reportDetail, Member member, PostReview review) {
        this.INAPPROPRIATE = INAPPROPRIATE;
        this.MISINFORMATION = MISINFORMATION;
        this.HATE = HATE;
        this.ABUSIVE = ABUSIVE;
        this.COPYRIGHT = COPYRIGHT;
        this.SPAM = SPAM;
        this.reportDetail = reportDetail;
        this.member = member;
        this.review = review;
    }

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
