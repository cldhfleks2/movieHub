package com.cldhfleks2.moviehub.moviereview;

import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.movie.Movie;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MovieReviewDTO {
    private Long movieReviewId;
    private String movieCd;
    private String content; //리뷰 내용
    private Double ratingValue; //리뷰 별점

    private String movieNm; //영화 이름
    private String moviePosterURL; //영화 포스터
    private LocalDateTime reviewUpdateDate; //리뷰 작성날짜
    private int likeCount; //리뷰의 총 좋아요 갯수
    private String authorNickname; //리뷰 작성자 닉네임
    private String authorProfileImage; //리뷰 작성자 사진

    private Long authorMemberId; //작성자의 memberId

    private Boolean isLiked; //현재 사용자 auth.getName()가 좋아요를 누른 상태인지

    //관리자 페이지에서 사용
    private Long id; //영화 리뷰 아이디
    private Movie movie; //영화 리뷰에 사용된 영화
    private Member member; //영화 리뷰 작성자
}
