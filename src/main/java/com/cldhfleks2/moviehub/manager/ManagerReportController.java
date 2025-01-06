package com.cldhfleks2.moviehub.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ManagerReportController { //신고 관리 페이지 전용 컨트롤러
    private final ManagerReportService managerReportService;

    //신고 관리 페이지 GET
    @GetMapping("/manager/report")
    String getManagerReport() {
        return managerReportService.getManagerReport();
    }
    
    //영화 신고 뷰 GET
    @GetMapping("/api/manager/report/movie")
    String getReportMovie(Model model, Integer pageIdx, String searchType, String keyword) {
        return managerReportService.getReportMovie(model, pageIdx, searchType, keyword);
    }

    //영화 리뷰 신고 뷰 GET
    @GetMapping("/api/manager/report/movieReview")
    String getReportMovieReview(Model model, Integer pageIdx, String searchType, String keyword) {
        return managerReportService.getReportMovieReview(model, pageIdx, searchType, keyword);
    }

    //게시글 신고 뷰 GET
    @GetMapping("/api/manager/report/post")
    String getReportPost(Model model, Integer pageIdx, String searchType, String keyword) {
        return managerReportService.getReportPost(model, pageIdx, searchType, keyword);
    }
}
