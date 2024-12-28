package com.cldhfleks2.moviehub.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieReviewLikeService {
    private final MovieReviewLikeRepository movieReviewLikeRepository;
}
