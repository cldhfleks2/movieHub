package com.cldhfleks2.moviehub.review;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovieReviewDTO {
    private String movieCd;
    private String content;
    private Double ratingValue;
}
