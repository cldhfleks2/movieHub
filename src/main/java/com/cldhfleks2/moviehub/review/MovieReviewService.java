package com.cldhfleks2.moviehub.review;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieReviewService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final MovieReviewRepository movieReviewRepository;
    private final MovieReviewLikeRepository movieReviewLikeRepository;

    //영화 리뷰 페이지 GET
    String getMovieReview(String movieCd, Model model, Authentication auth, Integer pageIdx) {
        if(pageIdx == null) pageIdx = 1;

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "", "유저 정보를 찾을 수 없습니다.", String.class);

        //movieCd가 전달 되었으면 movie를 찾아서 보내줌
        //영화 상세 페이지등에서 넘어올때는 movieCd값을 전달하도록 했음
        if(movieCd != null && !movieCd.isEmpty()){
            Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
            if(!movieObj.isPresent()) //영화 존재 여부 체크
                return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/movieReview", "영화 정보를 찾을 수 없습니다.", String.class);
            Movie movie = movieObj.get();
            model.addAttribute("movie", movie);
        }

        //리뷰 목록을 보여줌 : 한페이지에 5개 보여주도록
        Page<MovieReview> movieReviewList = movieReviewRepository.findAllByMovieCdAndStatus(movieCd, PageRequest.of(pageIdx - 1, 5));
        List<MovieReviewDTO> movieReviewDTOList = new ArrayList<>();
        for (MovieReview movieReview : movieReviewList) {
            //리뷰 총 좋아요 가져오기
            List<MovieReviewLike> movieReviewLikeList = movieReviewLikeRepository.findAllByMovieReviewIdAndStatus(movieReview.getId());
            int likeCount = movieReviewLikeList.size();
            //내가 좋아요를 눌렀는지 상태 가져오기
            Long movieReviewId = movieReview.getId();
            Optional<MovieReviewLike> movieReviewLikeObj = movieReviewLikeRepository.findByUsernameAndMovieReviewId(username, movieReviewId);
            Boolean isLiked;
            if(movieReviewLikeObj.isPresent()){
                int status = movieReviewLikeObj.get().getStatus();
                isLiked = (status == 1); //상태 저장
            }else{
                isLiked = false; //좋아요 안누름 상태 저장
            }
            MovieReviewDTO movieReviewDTO = MovieReviewDTO.builder()
                    .content(movieReview.getContent())
                    .ratingValue(movieReview.getRatingValue())
                    .movieNm(movieReview.getMovie().getMovieNm())
                    .moviePosterURL(movieReview.getMovie().getPosterURL())
                    .reviewUpdateDate(movieReview.getUpdateDate())
                    .likeCount(likeCount)
                    .authorNickname(movieReview.getMember().getNickname())
                    .authorProfileImage(movieReview.getMember().getProfileImage())
                    .isLiked(isLiked) //내가 좋아요를 눌렀는지 보냄
                    .build();
            movieReviewDTOList.add(movieReviewDTO); //전체 DTO 리스트에 추가
        }

        //페이지로 전달
        Page<MovieReviewDTO> movieReviewDTOPage = new PageImpl<>(
                movieReviewDTOList,
                movieReviewList.getPageable(),
                movieReviewList.getTotalElements()
        );

        model.addAttribute("movieReviewDTOPage",movieReviewDTOPage);

        //페이지네이션사용시 현재 컨트롤러를 다시 호출하므로 필요한 값.
        model.addAttribute("movieCd", movieCd);

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
