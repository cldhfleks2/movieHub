package com.cldhfleks2.moviehub.like;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MovieLikeController {
    private final MovieLikeService movieLikeService;

    //영화 상세 페이지에서 좋아요 버튼 눌렀을때 detail페이지도 전송
    @PostMapping("/api/movieDetail/like")
    String addMovieLike(String movieCd, Model model, Authentication auth) {
        return movieLikeService.addMovieLike(movieCd, model, auth);
    }

    //찜한 영화 삭제 요청 : mywish페이지를 전달할 것인지 check
    @DeleteMapping("/api/remove/movielike")
    String removeLike(String movieCd, Integer pageIdx, Model model, Boolean render, Authentication auth) throws Exception{
        return movieLikeService.removeLike(movieCd, pageIdx, model, render, auth);
    }


}
