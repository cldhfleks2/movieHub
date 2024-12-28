package com.cldhfleks2.moviehub.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    //리뷰 신고 요청
    @PostMapping("/api/movieReview/report")
    ResponseEntity<String> saveMovieReviewReport(MovieReviewReportDTO movieReviewReportDTO, Authentication auth) {
        return reportService.saveMovieReviewReport(movieReviewReportDTO, auth);
    }

    //영화 신고 요청
    @PostMapping("/api/movie/report")
    ResponseEntity<String> saveMovieReport(MovieReportDTO movieReportDTO, Authentication auth) {
        return reportService.saveMovieReport(movieReportDTO, auth);
    }
}
