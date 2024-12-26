package com.cldhfleks2.moviehub.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieLikeService {
    private final MovieLikeRepository movieLikeRepository;

}
