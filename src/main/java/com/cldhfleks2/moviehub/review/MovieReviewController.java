package com.cldhfleks2.moviehub.review;

import com.cldhfleks2.moviehub.report.MovieReviewReportDTO;
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
    String getMovieReview(Model model, Authentication auth, String searchText, Integer pageIdx, String dateSort, String ratingSort, String movieCd) {
        return movieReviewService.getMovieReview(model, auth, searchText, pageIdx, dateSort, ratingSort, movieCd);
    }

    //영화 리뷰 작성 내용을 서버에 저장
    @PostMapping("/api/movieReview/add")
    ResponseEntity<String> addMovieReview(@RequestBody MovieReviewDTO movieReviewDTO, Model model, Authentication auth) {
        return movieReviewService.addMovieReview(movieReviewDTO, model, auth);
    }

    //리뷰 좋아요 요청
    @PostMapping("/api/movieReview/like")
    ResponseEntity<String> addMovieReviewLike(Long reviewId, Authentication auth) {
        return movieReviewService.addMovieReviewLike(reviewId, auth);
    }

    //리뷰 신고 요청
    @PostMapping("/api/movieReview/report")
    ResponseEntity<String> addMovieReviewReport(MovieReviewReportDTO movieReviewReportDTO, Authentication auth) {
        return movieReviewService.addMovieReviewReport(movieReviewReportDTO, auth);
    }



}
