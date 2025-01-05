package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.movie.MovieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;

    //영화 관리자 페이지 GET
    @GetMapping("/manager/movie")
    String getManagerMovie() {
        return managerService.getManagerMovie();
    }

    //영화 관리자 페이지 : 검색 결과 뷰 GET
    @GetMapping("/api/manager/movie/search")
    String searchMovie(Model model, Integer pageIdx, String keyword){
        return managerService.searchMovie(model, pageIdx, keyword);
    }

    //영화 관리자 페이지 : 영화 상세 정보 뷰 GET
    @GetMapping("/api/manager/movie/get")
    String getMovieDTO(Model model, Long movieId){
        return managerService.getMovieDTO(model, movieId);
    }

    //영화 관리자 페이지 : 영화 정보 수정 요청
    @PatchMapping("/api/manager/movie/edit")
    ResponseEntity<String> editMovie(@RequestBody MovieDTO movieDTO) {
        return managerService.editMovie(movieDTO);
    }

    //영화 관리자 페이지 : 영화 포스터 수정 요청
    @PatchMapping("/api/manager/movie/edit/img")
    ResponseEntity<String> editMoviePoster(@RequestParam MultipartFile posterImg, @RequestParam Long movieId) throws Exception{
        return managerService.editMoviePoster(posterImg, movieId);
    }

    //영화 리뷰 관리자 페이지 GET
    @GetMapping("/manager/movieReview")
    String getMovieReview() {
        return managerService.getMovieReview();
    }

    //영화 리뷰 관리자 페이지 : 리뷰 검색
    @GetMapping("/api/manager/movieReview/search")
    String searchMovieReview(Model model, Integer pageIdx, String keyword){
        return managerService.searchMovieReview(model, pageIdx, keyword);
    }

    //영화 리뷰 관리자 페이지 : 영화 리뷰 상세 검색
    @GetMapping("/api/manager/movieReview/detail")
    String getMovieReviewDetail(Model model, Long reviewId){
        return managerService.getMovieReviewDetail(model, reviewId);
    }

    //영화 리뷰 관리자 페이지 : 영화 리뷰 삭제
    @DeleteMapping("/api/manager/movieReview/delete")
    ResponseEntity<String> deleteMovieReview(Long reviewId){
        return managerService.deleteMovieReview(reviewId);
    }

    //영화 리뷰 관리자 페이지 : 영화 리뷰 수정
    @PatchMapping("/api/manager/movieReview/edit")
    ResponseEntity<String> editMovieReview(Long reviewId, String content) {
        return managerService.editMovieReview(reviewId, content);
    }

    //게시글 관리 페이지 GET
    @GetMapping("/manager/post")
    String getPost() {
        return managerService.getPost();
    }

    //게시글 관리 페이지 : 게시글 상세 검색
    @GetMapping("/api/manager/post/detail")
    String searchPostDetail(Model model, Authentication auth, Long postId) {
        return managerService.searchPostDetail(model, auth, postId);
    }

}
