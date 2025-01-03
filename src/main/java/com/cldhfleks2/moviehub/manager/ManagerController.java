package com.cldhfleks2.moviehub.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;

    //관리자 페이지 GET
    @GetMapping("/manager/movie")
    String getManager(Authentication auth, Model model) {
        return managerService.getManager(auth, model);
    }

    //영화 검색 결과 뷰 GET
    @GetMapping("/api/manager/movie/search")
    String searchMovie(Model model, Integer pageIdx, String keyword){
        return managerService.searchMovie(model, pageIdx, keyword);
    }

    //영화 상세 정보 뷰 GET
    @GetMapping("/api/manager/movie/get")
    String getMovieDTO(Model model, Long movieId){
        return managerService.getMovieDTO(model, movieId);
    }



}
