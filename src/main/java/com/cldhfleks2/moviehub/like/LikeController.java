package com.cldhfleks2.moviehub.like;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    //영화 리뷰 좋아요 요청
    @PostMapping("/api/movieReview/like")
    ResponseEntity<String> addMovieReviewLike(Long reviewId, Authentication auth) {
        return likeService.addMovieReviewLike(reviewId, auth);
    }

    //영화 좋아요 요청
    //영화 상세 페이지에서 좋아요 버튼 눌렀을때 detail페이지도 전송
    @PostMapping("/api/movieDetail/like")
    String addMovieLike(String movieCd, Model model, Authentication auth) {
        return likeService.addMovieLike(movieCd, model, auth);
    }

    //영화 좋아요 삭제 요청 : mywish페이지를 전달할 것인지 check
    @DeleteMapping("/api/remove/movielike")
    String removeMovieLike(String movieCd, Integer pageIdx, Model model, Boolean render, Authentication auth) throws Exception{
        return likeService.removeMovieLike(movieCd, pageIdx, model, render, auth);
    }

    //댓글 좋아요 요청 : save or status toggle
    @PostMapping("/api/post/review/like")
    ResponseEntity<String> likePostReview(Long reviewId, Authentication auth){
        return likeService.likePostReview(reviewId, auth);
    }

    //게시글 좋아요 요청 : save or status toggle
    @PostMapping("/api/post/like")
    ResponseEntity<String> likePost(Long postId, Authentication auth){
        return likeService.likePost(postId, auth);
    }

}
