package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.report.movie.MovieReport;
import com.cldhfleks2.moviehub.report.movie.MovieReportDTO;
import com.cldhfleks2.moviehub.report.movie.MovieReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerReportService {
    private final MovieReportRepository movieReportRepository;

    //신고 관리 페이지 GET
    String getManagerReport() {
        return "manager/report";
    }

    //영화 신고 뷰 GET
    String getReportMovie(Model model, Integer pageIdx, String searchType, String keyword) {
        if (pageIdx == null) pageIdx = 1;
        if (searchType == null) searchType = "reporter";
        if (keyword == null) keyword = "";

        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(pageIdx-1, pageSize);

        Page<MovieReport> movieReportPage;
        if(searchType.equals("reporter"))
            movieReportPage = movieReportRepository.findReportDetailByKeyword(keyword, pageRequest);
        else
            movieReportPage = movieReportRepository.findMovieNmByKeyword(keyword, pageRequest);

        List<MovieReportDTO> movieReportDTOList = new ArrayList<>();
        for(MovieReport movieReport : movieReportPage.getContent()){
            MovieReportDTO movieReportDTO = MovieReportDTO.builder()
                    .POSTER(movieReport.getPOSTER())
                    .MOVIENAME(movieReport.getMOVIENAME())
                    .MOVIEPEOPLE(movieReport.getMOVIEPEOPLE())
                    .HARMFUL(movieReport.getHARMFUL())
                    .HATE(movieReport.getHATE())
                    .reportDetail(movieReport.getReportDetail())
                    .movie(movieReport.getMovie())
                    .member(movieReport.getMember())
                    .updateDate(movieReport.getUpdateDate())
                    .status(movieReport.getStatus() == 1)
                    .build();
            movieReportDTOList.add(movieReportDTO);
        }

        //페이지로 전달
        Page<MovieReportDTO> searchPage = new PageImpl<>(
                movieReportDTOList,
                movieReportPage.getPageable(),
                movieReportPage.getTotalElements()
        );
        model.addAttribute("movieReportPage", searchPage);

        return "manager/report :: #movieContent";
    }

}
