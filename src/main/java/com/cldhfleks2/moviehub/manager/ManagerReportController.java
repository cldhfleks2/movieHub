package com.cldhfleks2.moviehub.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
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
}
