package com.cldhfleks2.moviehub.report;

import com.cldhfleks2.moviehub.report.movie.MovieReportDTO;
import com.cldhfleks2.moviehub.report.movie.MovieReviewReportDTO;
import com.cldhfleks2.moviehub.report.post.PostReportDTO;
import com.cldhfleks2.moviehub.report.post.PostReviewReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    //영화 리뷰 신고 요청
    @PostMapping("/api/movieReview/report")
    ResponseEntity<String> saveMovieReviewReport(MovieReviewReportDTO movieReviewReportDTO, Authentication auth) {
        return reportService.saveMovieReviewReport(movieReviewReportDTO, auth);
    }

    //영화 신고 요청
    @PostMapping("/api/movie/report")
    ResponseEntity<String> saveMovieReport(MovieReportDTO movieReportDTO, Authentication auth) {
        return reportService.saveMovieReport(movieReportDTO, auth);
    }

    //게시글 신고 요청
    @PostMapping("/api/post/report")
    ResponseEntity<String> savePostReport(PostReportDTO postReportDTO, Authentication auth){
        return reportService.savePostReport(postReportDTO, auth);
    }

    //댓글 신고 요청
    @PostMapping("/api/post/review/report")
    ResponseEntity<String> savePostReviewReport(PostReviewReportDTO postReviewReportDTO, Authentication auth){
        return reportService.savePostReviewReport(postReviewReportDTO, auth);
    }

}
