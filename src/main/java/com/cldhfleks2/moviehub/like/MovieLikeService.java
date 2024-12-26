package com.cldhfleks2.moviehub.like;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieLikeService {
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;


    //영화 상세 페이지에서 좋아요 버튼 눌렀을때
    @GetMapping("/api/movieDetail/like")
    String clickLikeBtn(String movieCd, Model model, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieDetail/like", "유저 정보를 찾을 수 없습니다.", String.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieDetail/like", "영화 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        Movie movie = movieObj.get();
        Boolean likeStatus;
        Optional<MovieLike> movieLikeObj = movieLikeRepository.findByUsername(username);
        if(!movieLikeObj.isPresent()){ //처음 눌렀을때 : DB에 추가
            MovieLike movieLike = new MovieLike();
            movieLike.setMember(member);
            movieLike.setMovie(movie);
            movieLikeRepository.save(movieLike);
            likeStatus = true; //상태 저장
        }else{ //기존에 눌렀어서 DB에 남아있을때
            MovieLike movieLike = movieLikeObj.get();
            int status = movieLike.getStatus();
            status = (status + 1) % 2; //toggle
            movieLike.setStatus(status);
            movieLikeRepository.save(movieLike); //수정
            likeStatus = (status == 1); //상태 저장
        }
        model.addAttribute("likeStatus", likeStatus);
        
        //총 갯수 구하기
        List<MovieLike> movieLikeList = movieLikeRepository.findAllByMovieCdAndStatus(movieCd);
        int totalLikeCnt = (movieLikeList != null) ? movieLikeList.size() : 0;
        model.addAttribute("totalLikeCnt", totalLikeCnt);

        return "detail/detail";
    }

}
