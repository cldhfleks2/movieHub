package com.cldhfleks2.moviehub.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class MemberDTO {
    private Long id;
    private String username;
    private String nickname;
    private String profileImage;
    
    //메인페이지의 활동량 많은 유저 보여줄때 사용
    private Integer totalScore;
    private Integer movieReviewCount; //영화 리뷰 갯수
    private Integer postCount; //게시글 갯수
    private Integer postReviewCount; //게시글 댓글 갯수
}
