package com.cldhfleks2.moviehub.bookmark;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.movie.MovieLike;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;

    @Transactional
    String clickBookmarkBtn(String movieCd, Model model, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieDetail/bookmark", "유저 정보를 찾을 수 없습니다.", String.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieDetail/bookmark", "영화 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        Movie movie = movieObj.get();
        Optional<BookMark> bookmarkObj = bookMarkRepository.findByUsernameAndMovieCd(username, movieCd);
        Boolean bookmarkStatus;
        if(!bookmarkObj.isPresent()){ //최초로 클릭한경우 : DB에 생성
            BookMark bookmark = new BookMark();
            bookmark.setMember(member);
            bookmark.setMovie(movie);
            bookMarkRepository.save(bookmark); //저장
            bookmarkStatus = true;
        }else{
            BookMark bookmark = bookmarkObj.get();
            int status = bookmark.getStatus();
            status = (status + 1)%2; //toggle
            bookmark.setStatus(status);
            bookMarkRepository.save(bookmark); //수정
            bookmarkStatus = (status == 1); //상태 저장
        }
        model.addAttribute("bookmarkStatus", bookmarkStatus);

        //movieCd값을 사용해야 하므로 모델로 전달
        MovieDTO movieDetail = MovieDTO.create().movieCd(movieCd).build();
        model.addAttribute("movieDetail", movieDetail);

        return "detail/detail";
    }

    //내가 찜한 영화 리스트 GET
    public String getMyWish(Model model, Authentication auth, Integer pageIdx) throws Exception{
        if(pageIdx == null) pageIdx = 1;

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mywish", "유저 정보를 찾을 수 없습니다.", String.class);

        //좋아요한 전체 영화 객체를 가져오기
        Page<MovieLike> movieLikePage = movieLikeRepository.findAllByUsernameAndStatus(username, PageRequest.of(pageIdx - 1, 8));
        List<Movie> movieList = new ArrayList<>();
        for (MovieLike movieLike : movieLikePage)
            movieList.add(movieLike.getMovie());

        //movieDTO만들기
        List<MovieDTO> movieDTOList = new ArrayList<>();
        for(Movie movie : movieList) {
            MovieDTO movieDTO = movieService.getMovieDTO(movie);
            movieDTOList.add(movieDTO);
        }

        //페이지로 전달
        Page<MovieDTO> movieDTOPage = new PageImpl<>(
                movieDTOList,
                movieLikePage.getPageable(),
                movieLikePage.getTotalElements()
        );

        model.addAttribute("movieDTOPage", movieDTOPage);

        return "member/mywish";
    }

}
