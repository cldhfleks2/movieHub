package com.cldhfleks2.moviehub.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MovieReviewLikeController {
    private final MovieReviewLikeService movieReviewLikeService;
}
