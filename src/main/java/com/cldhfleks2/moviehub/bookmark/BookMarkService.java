package com.cldhfleks2.moviehub.bookmark;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    public String getMyWish(Model model, Authentication auth, Integer pageIdx, String sort) throws Exception{
        if(pageIdx == null) pageIdx = 1;
        if(sort == null) sort = "latest"; //sort : title,latest

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mywish", "유저 정보를 찾을 수 없습니다.", String.class);

        //찜한 전체 영화 가져오기 : 정렬하여 가져오기
        Sort sortOrder;
        if(sort.equals("title"))
            sortOrder = Sort.by(Sort.Order.asc("movie.movieNm"));
        else if(sort.equals("latest"))
            sortOrder = Sort.by(Sort.Order.desc("updateDate"));
        else
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mywish", "정렬 기준이 잘못 되었습니다.", String.class);
        int pageSize = 4; //한페이지에 4개씩 보여줄것

        Pageable pageable = PageRequest.of(pageIdx - 1, pageSize, sortOrder);
        Page<BookMark> bookMarkPage = bookMarkRepository.findAllByUsernameAndStatus(username, pageable);

        //movieDTO만들기
        List<MovieDTO> movieDTOList = new ArrayList<>();
        for (BookMark bookMark : bookMarkPage){
            Movie movie = bookMark.getMovie();
            MovieDTO movieDTO = movieService.getMovieDTO(movie);
            movieDTO.setBookmarkUpdateDate(bookMark.getUpdateDate());
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
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/bookmark/add", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/bookmark/add", "영화 정보를 찾을 수 없습니다.", ResponseEntity.class);

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

    //찜한 영화 삭제 요청
    @Transactional
    ResponseEntity<String> deleteBookmark(String movieCd, Authentication auth){
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/bookmark/delete", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCdAndStatus(movieCd);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/bookmark/delete", "영화 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<BookMark> bookMarkObj = bookMarkRepository.findByUsernameAndMovieCd(username, movieCd);
        if(!bookMarkObj.isPresent())
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/bookmark/delete", "찜한 정보를 찾을 수 없습니다.", ResponseEntity.class);

        BookMark bookmark = bookMarkObj.get();
        int status = bookmark.getStatus();
        status = (status - 1) % 2;
        bookmark.setStatus(status);
        bookMarkRepository.save(bookmark); //update

        //204 : 정상삭제 응답 보내기
        return ResponseEntity.noContent().build();
    }

}
