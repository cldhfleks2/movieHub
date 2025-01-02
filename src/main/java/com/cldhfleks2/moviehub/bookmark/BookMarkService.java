package com.cldhfleks2.moviehub.bookmark;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final MovieService movieService;

    //내가 찜한 영화 리스트 GET
    public String getMyWish(Model model, Authentication auth, Integer pageIdx) throws Exception{
        if(pageIdx == null) pageIdx = 1;

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mywish", "유저 정보를 찾을 수 없습니다.", String.class);

        //좋아요한 전체 영화 객체를 가져오기
        Page<BookMark> bookMarkPage = bookMarkRepository.findAllByUsernameAndStatus(username, PageRequest.of(pageIdx - 1, 8));
        List<Movie> movieList = new ArrayList<>();
        for (BookMark bookMark : bookMarkPage)
            movieList.add(bookMark.getMovie());

        //movieDTO만들기
        List<MovieDTO> movieDTOList = new ArrayList<>();
        for(Movie movie : movieList) {
            MovieDTO movieDTO = movieService.getMovieDTO(movie);
            movieDTOList.add(movieDTO);
        }

        //페이지로 전달
        Page<MovieDTO> movieDTOPage = new PageImpl<>(
                movieDTOList,
                bookMarkPage.getPageable(),
                bookMarkPage.getTotalElements() == 0 ? 1 : bookMarkPage.getTotalElements()
        );
        
        //찜한 영화 목록을 보냄
        model.addAttribute("movieDTOPage", movieDTOPage);

        return "member/mywish";
    }

    //찜한 영화 추가 요청 : 영화 상세 페이지에서 찜하기 버튼 눌렀을때
    @Transactional
    ResponseEntity<String> addBookmark(String movieCd, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieDetail/bookmark", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieDetail/bookmark", "영화 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Movie movie = movieObj.get();
        Optional<BookMark> bookmarkObj = bookMarkRepository.findByUsernameAndMovieCd(username, movieCd);
        if(!bookmarkObj.isPresent()){ //최초로 클릭한경우 : DB에 생성
            BookMark bookmark = new BookMark();
            bookmark.setMember(member);
            bookmark.setMovie(movie);
            bookMarkRepository.save(bookmark); //저장
        }else{
            BookMark bookmark = bookmarkObj.get();
            int status = bookmark.getStatus();
            status = (status + 1)%2; //toggle
            bookmark.setStatus(status);
            bookMarkRepository.save(bookmark); //수정
        }

        return ResponseEntity.ok().build();
    }



}
