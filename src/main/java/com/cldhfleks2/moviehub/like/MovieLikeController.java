package com.cldhfleks2.moviehub.like;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MovieLikeController {
    private final MovieLikeService movieLikeService;

    //영화 상세 페이지에서 좋아요 버튼 눌렀을때
    @PostMapping("/api/movieDetail/like")
    String clickLikeBtn(String movieCd, Model model, Authentication auth) {
        return movieLikeService.clickLikeBtn(movieCd, model, auth);
    }


}
