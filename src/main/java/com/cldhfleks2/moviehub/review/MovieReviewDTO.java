package com.cldhfleks2.moviehub.review;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MovieReviewDTO {
    private String movieCd;
    private String content; //리뷰 내용
    private Double ratingValue; //리뷰 별점

    private String movieNm; //영화 이름
    private String moviePosterURL; //영화 포스터
    private LocalDateTime reviewUpdateDate; //리뷰 작성날짜
    private int likeCount; //리뷰의 총 좋아요 갯수
    private String authorNickname; //리뷰 작성자 닉네임
    private String authorProfileImage; //리뷰 작성자 사진

    private Boolean isLiked; //현재 사용자 auth.getName()가 좋아요를 누른 상태인지
}
