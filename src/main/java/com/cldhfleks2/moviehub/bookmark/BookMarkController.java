package com.cldhfleks2.moviehub.bookmark;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService;

    //영화 상세 페이지에서 북마크 버튼 눌렀을때
    @PostMapping("/api/movieDetail/bookmark")
    String clickBookmarkBtn(String movieCd, Model model, Authentication auth) {
        return bookMarkService.clickBookmarkBtn(movieCd, model, auth);
    }
}
