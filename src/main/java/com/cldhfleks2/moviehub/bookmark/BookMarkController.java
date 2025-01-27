package com.cldhfleks2.moviehub.bookmark;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService;

    //내가 찜한 영화 리스트 GET
    @GetMapping("/mywish")
    String getMyWish(Model model, Authentication auth, Integer pageIdx, String sort) throws Exception{
        return bookMarkService.getMyWish(model, auth, pageIdx, sort);
    }

    //찜한 영화 추가 요청 : 영화 상세 페이지에서 찜하기 버튼 눌렀을때
    @PostMapping("/api/bookmark/add")
    ResponseEntity<String> addBookmark(String movieCd, Authentication auth) {
        return bookMarkService.addBookmark(movieCd, auth);
    }

    //찜한 영화 삭제 요청
    @DeleteMapping("/api/bookmark/delete")
    ResponseEntity<String> deleteBookmark(String movieCd, Authentication auth){
        return bookMarkService.deleteBookmark(movieCd, auth);
    }

}
