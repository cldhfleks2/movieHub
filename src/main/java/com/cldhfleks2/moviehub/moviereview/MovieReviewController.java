package com.cldhfleks2.moviehub.moviereview;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class MovieReviewController {
    private final MovieReviewService movieReviewService;

    //영화 리뷰 페이지 GET
    @GetMapping("/movieReview")
    String getMovieReview(Model model, Authentication auth, String searchText, Integer pageIdx, String dateSort, String ratingSort, String movieCd) {
        return movieReviewService.getMovieReview(model, auth, searchText, pageIdx, dateSort, ratingSort, movieCd);
    }

    //영화 리뷰 작성 내용을 서버에 저장
    @PostMapping("/api/movieReview/add")
    ResponseEntity<String> addMovieReview(@RequestBody MovieReviewDTO movieReviewDTO, Model model, Authentication auth) {
        return movieReviewService.addMovieReview(movieReviewDTO, model, auth);
    }

    //영화 리뷰 삭제 요청
    @DeleteMapping("/api/movieReview/delete")
    ResponseEntity<String> deleteMovieReview(Long reviewId, Authentication auth) {
        return movieReviewService.deleteMovieReview(reviewId, auth);
    }
}
