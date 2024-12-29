package com.cldhfleks2.moviehub.like;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.member.MemberService;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieLikeService {
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final MemberService memberService;


    //영화 상세 페이지에서 좋아요 버튼 눌렀을때 detail페이지도 전송
    @Transactional
    String addMovieLike(String movieCd, Model model, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieDetail/like", "유저 정보를 찾을 수 없습니다.", String.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieDetail/like", "영화 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        Movie movie = movieObj.get();
        Boolean likeStatus;
        Optional<MovieLike> movieLikeObj = movieLikeRepository.findByUsernameAndMovieCd(username, movieCd);
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

        //movieCd값 전달을 위해 MovieDTO선언
        MovieDTO movieDetail = MovieDTO.builder().movieCd(movieCd).build();
        model.addAttribute("movieDetail", movieDetail);

        return "detail/detail";
    }

    //찜한 영화 삭제 요청 : mywish페이지를 전달할 것인지 check
    @Transactional
    String removeLike(String movieCd, Integer pageIdx, Model model, Boolean render, Authentication auth) throws Exception {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/remove/movielike", "유저 정보를 찾을 수 없습니다.", String.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/remove/movielike", "영화 정보를 찾을 수 없습니다.", String.class);

        //찜한리스트에서 삭제
        Optional<MovieLike> movieLikeObj = movieLikeRepository.findByUsernameAndMovieCd(username, movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/remove/movielike", "좋아요 내역을 찾을 수 없습니다.", String.class);
        MovieLike movieLike = movieLikeObj.get();
        movieLikeRepository.delete(movieLike); //해당 좋아요 내역 삭제

        //화면을 렌더링 할것인지..
        if(render)
            return memberService.getMyWish(model, auth, pageIdx);
        else
            return null;
    }









}
