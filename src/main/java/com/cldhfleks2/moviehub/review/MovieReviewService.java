package com.cldhfleks2.moviehub.review;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieReviewService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final MovieReviewRepository movieReviewRepository;

    //영화 리뷰 페이지 GET
    String getMovieReview(String movieCd, Model model, Authentication auth) {
        //영화 상세 페이지등에서 넘어올때는 movieCd값을 전달하도록 했음
        //전달되있으면 해당 영화에 대한 정보를 넘겨줄것임
        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/movieReview", "영화 정보를 찾을 수 없습니다.", String.class);
        Movie movie = movieObj.get();
        model.addAttribute("movie", movie);

        return "review/review";
    }

    //영화 리뷰 작성 내용을 서버에 저장
    ResponseEntity<String> addMovieReview(MovieReviewDTO movieReviewDTO, Model model, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieReview/add", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCd(movieReviewDTO.getMovieCd());
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieReview/add", "영화 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Movie movie = movieObj.get();
        MovieReview movieReview = new MovieReview();
        movieReview.setMovie(movie);
        movieReview.setMember(member);
        movieReview.setContent(movieReviewDTO.getContent());
        movieReview.setRatingValue(movieReviewDTO.getRatingValue());
        movieReviewRepository.save(movieReview); //리뷰 저장

        return ResponseEntity.status(HttpStatus.OK).build();
    }




}
