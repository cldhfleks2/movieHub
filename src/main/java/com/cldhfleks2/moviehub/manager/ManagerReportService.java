package com.cldhfleks2.moviehub.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerReportService {

    //신고 관리 페이지 GET
    String getManagerReport() {
        return "manager/report";
    }

}
