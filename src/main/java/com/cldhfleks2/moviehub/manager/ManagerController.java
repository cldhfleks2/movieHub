package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.movie.MovieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;

    //영화 관리자 페이지 GET
    @GetMapping("/manager/movie")
    String getManagerMovie(Authentication auth, Model model) {
        return managerService.getManagerMovie(auth, model);
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

    //영화 정보 수정 요청
    @PatchMapping("/api/manager/movie/edit")
    ResponseEntity<String> editMovie(@RequestBody MovieDTO movieDTO) {
        return managerService.editMovie(movieDTO);
    }

    //영화 포스터 수정 요청
    @PatchMapping("/api/manager/movie/edit/img")
    ResponseEntity<String> editMoviePoster(@RequestParam MultipartFile posterImg,
                                           @RequestParam Long movieId) throws Exception{
        return managerService.editMoviePoster(posterImg, movieId);
    }

}
