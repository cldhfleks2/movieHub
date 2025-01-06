package com.cldhfleks2.moviehub.report.post;

import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class PostReportDTO { //신고오버레이를 같은 항목을 사용해서 PostReviewReportDTO와 같다.
    private Long postId;

    private Boolean INAPPROPRIATE;   // 부적절하거나 불건전한 내용
    private Boolean MISINFORMATION; // 허위정보 또는 사실 왜곡
    private Boolean HATE;           // 혐오 발언/차별적 내용
    private Boolean ABUSIVE;        // 욕설 또는 부적절한 언행
    private Boolean COPYRIGHT;      // 저작권 침해 의심
    private Boolean SPAM;           // 광고 또는 스팸성 게시물

    private String reportDetail;

    private Post post;
    private Member member;
}
