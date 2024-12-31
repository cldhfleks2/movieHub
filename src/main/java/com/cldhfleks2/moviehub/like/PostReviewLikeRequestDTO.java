package com.cldhfleks2.moviehub.like;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class PostReviewLikeRequestDTO {
    private Long reviewId;
}
