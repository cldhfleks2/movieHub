package com.cldhfleks2.moviehub.report.movie;

import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.moviereview.MovieReview;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MovieReviewReportDTO {
    private Long movieReviewId;

    private Boolean SPOILER;   // 주요 스포일러 포함
    private Boolean WRONG;     // 잘못된 정보
    private Boolean UNRELATED; // 무관한 내용/광고성
    private Boolean HARMFUL;   // 유해하거나 불건전한 내용
    private Boolean HATE;      // 혐오 발언
    private Boolean COPYRIGHT; // 저작권 침해
    private Boolean SPAM;      // 개인정보 노출

    //신고 관리자 페이지에서 사용
    private String reportDetail;
    private Member member; //신고한 유저
    private MovieReview movieReview; //신고할 리뷰
    private LocalDateTime updateDate;
    private Boolean status;
}