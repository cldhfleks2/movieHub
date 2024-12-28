package com.cldhfleks2.moviehub.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class MovieReviewController {
    private final MovieReviewService movieReviewService;

    //영화 리뷰 페이지 GET
    @GetMapping("/movieReview")
    String getMovieReview(String movieCd, Model model, Authentication auth, Integer pageIdx) {
        return movieReviewService.getMovieReview(movieCd, model, auth, pageIdx);
    }

    //영화 리뷰 작성 내용을 서버에 저장
    @PostMapping("/api/movieReview/add")
    ResponseEntity<String> addMovieReview(@RequestBody MovieReviewDTO movieReviewDTO, Model model, Authentication auth) {
        return movieReviewService.addMovieReview(movieReviewDTO, model, auth);
    }

}
