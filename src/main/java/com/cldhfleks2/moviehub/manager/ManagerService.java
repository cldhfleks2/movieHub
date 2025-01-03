package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    String getManager(Authentication authentication, Model model) {


        return "manager/manager";
    }

    //영화 검색 결과 뷰 GET
    String searchMovie(Model model, Integer pageIdx, String keyword){
        if(pageIdx == null) pageIdx = 1;
        if(keyword == null) keyword = "";

        int pageSize = 10;
        Page<Movie> searchMoviePage = movieRepository.findByMovieNmAndStatus(keyword, PageRequest.of(pageIdx-1, pageSize));

        model.addAttribute("searchMovieList", searchMoviePage);

        return "manager/manager :: #searchResultSection";
    }

    //movieContent 뷰 GET
    String getMovieDTO(Model model, Long movieId){
        MovieDTO movieDTO = movieService.getMovieDTO(movieId);

        model.addAttribute("movie", movieDTO);

        return "manager/manager :: #movieContent";
    }
}
