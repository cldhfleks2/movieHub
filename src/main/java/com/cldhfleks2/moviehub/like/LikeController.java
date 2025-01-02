package com.cldhfleks2.moviehub.like;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
    @PostMapping("/api/movieDetail/like")
    ResponseEntity<String> addMovieLike(String movieCd, Authentication auth) {
        return likeService.addMovieLike(movieCd, auth);
    }

    //영화 좋아요 삭제 요청
    @DeleteMapping("/api/remove/movielike")
    ResponseEntity<String> removeMovieLike(String movieCd, Authentication auth) throws Exception{
        return likeService.removeMovieLike(movieCd, auth);
    }

    //게시글 댓글 좋아요 요청 : save or status toggle
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
