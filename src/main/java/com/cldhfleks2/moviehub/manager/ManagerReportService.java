package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.report.movie.*;
import com.cldhfleks2.moviehub.report.post.PostReport;
import com.cldhfleks2.moviehub.report.post.PostReportDTO;
import com.cldhfleks2.moviehub.report.post.PostReportRepository;
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
    private final MovieReviewReportRepository movieReviewReportRepository;
    private final PostReportRepository postReportRepository;

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

    //영화 리뷰 신고 뷰 GET
    String getReportMovieReview(Model model, Integer pageIdx, String searchType, String keyword) {
        if (pageIdx == null) pageIdx = 1;
        if (searchType == null) searchType = "reporter";
        if (keyword == null) keyword = "";

        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(pageIdx-1, pageSize);

        Page<MovieReviewReport> movieReviewReportPage;
        if(searchType.equals("reporter")) //신고 내용
            movieReviewReportPage = movieReviewReportRepository.findByReportDetailContaining(keyword, pageRequest);
        else if(searchType.equals("movieNm")) //영화 제목
            movieReviewReportPage = movieReviewReportRepository.findByMovieNmContaining(keyword, pageRequest);
        else if(searchType.equals("nickname")) //작성자 닉네임
            movieReviewReportPage = movieReviewReportRepository.findByMemberNicknameContaining(keyword, pageRequest);
        else //리뷰 내용
            movieReviewReportPage = movieReviewReportRepository.findByMovieReviewContentContaining(keyword, pageRequest);

        List<MovieReviewReportDTO> movieReviewReportDTOList = new ArrayList<>();
        for(MovieReviewReport movieReviewReport : movieReviewReportPage.getContent()){
            MovieReviewReportDTO movieReviewReportDTO = MovieReviewReportDTO.builder()
                    .SPOILER(movieReviewReport.getSPOILER())
                    .WRONG(movieReviewReport.getWRONG())
                    .UNRELATED(movieReviewReport.getUNRELATED())
                    .HARMFUL(movieReviewReport.getHARMFUL())
                    .HATE(movieReviewReport.getHATE())
                    .COPYRIGHT(movieReviewReport.getCOPYRIGHT())
                    .SPAM(movieReviewReport.getSPAM())
                    .reportDetail(movieReviewReport.getReportDetail())
                    .member(movieReviewReport.getMember())
                    .movieReview(movieReviewReport.getMovieReview())
                    .updateDate(movieReviewReport.getUpdateDate())
                    .status(movieReviewReport.getStatus() == 1)
                    .build();
            movieReviewReportDTOList.add(movieReviewReportDTO);
        }

        //페이지로 전달
        Page<MovieReviewReportDTO> searchPage = new PageImpl<>(
                movieReviewReportDTOList,
                movieReviewReportPage.getPageable(),
                movieReviewReportPage.getTotalElements()
        );
        model.addAttribute("movieReviewReportPage", searchPage);

        return "manager/report :: #movieReviewContent";
    }

    //게시글 신고 뷰 GET
    String getReportPost(Model model, Integer pageIdx, String searchType, String keyword) {
        if (pageIdx == null) pageIdx = 1;
        if (searchType == null) searchType = "reporter";
        if (keyword == null) keyword = "";

        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(pageIdx-1, pageSize);

        Page<PostReport> postReportPage;
        if(searchType.equals("reporter")) //신고 내용
            postReportPage = postReportRepository.findByReportDetailContaining(keyword, pageRequest);
        else if(searchType.equals("title")) //게시글 제목
            postReportPage = postReportRepository.findByPostTitleContaining(keyword, pageRequest);
        else if(searchType.equals("content")) //게시글 내용
            postReportPage = postReportRepository.findByPostContentContaining(keyword, pageRequest);
        else //작성자 닉네임
            postReportPage = postReportRepository.findByMemberNicknameContaining(keyword, pageRequest);

        List<PostReportDTO> postReportDTOList = new ArrayList<>();
        for(PostReport postReport : postReportPage.getContent()){
            PostReportDTO postReportDTO = PostReportDTO.create()
                    .INAPPROPRIATE(postReport.getINAPPROPRIATE())
                    .MISINFORMATION(postReport.getMISINFORMATION())
                    .HATE(postReport.getHATE())
                    .ABUSIVE(postReport.getABUSIVE())
                    .COPYRIGHT(postReport.getCOPYRIGHT())
                    .SPAM(postReport.getSPAM())
                    .reportDetail(postReport.getReportDetail())
                    .post(postReport.getPost())
                    .member(postReport.getMember())
                    .updateDate(postReport.getUpdateDate())
                    .status(postReport.getStatus() == 1)
                    .build();
            postReportDTOList.add(postReportDTO);
        }

        //페이지로 전달
        Page<PostReportDTO> searchPage = new PageImpl<>(
                postReportDTOList,
                postReportPage.getPageable(),
                postReportPage.getTotalElements()
        );

        model.addAttribute("postReportPage", searchPage);

        return "manager/report :: #postContent";
    }

}

