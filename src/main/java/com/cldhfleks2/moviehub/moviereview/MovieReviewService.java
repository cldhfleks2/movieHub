package com.cldhfleks2.moviehub.moviereview;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLike;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLikeRepository;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.notification.NotificationRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieReviewService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final MovieReviewRepository movieReviewRepository;
    private final MovieReviewLikeRepository movieReviewLikeRepository;
    private final NotificationRepository notificationRepository;
    
    //정렬 하는 함수
    List<MovieReviewDTO> sortReviews(List<MovieReviewDTO> reviews, String dateSort, String ratingSort) {
        // 날짜 정렬 기준 (recent, old)
        Comparator<MovieReviewDTO> dateComparator = (review1, review2) -> {
            // 날짜만 비교하기 위해 LocalDate로 변환
            LocalDate date1 = review1.getReviewUpdateDate().toLocalDate();
            LocalDate date2 = review2.getReviewUpdateDate().toLocalDate();

            if ("recent".equals(dateSort)) {
                return date2.compareTo(date1); // 최신순
            } else {
                return date1.compareTo(date2); // 오래된순
            }
        };

        // 별점 정렬 기준 (high, low)
        Comparator<MovieReviewDTO> ratingComparator = (review1, review2) -> {
            if ("high".equals(ratingSort)) {
                return review2.getRatingValue().compareTo(review1.getRatingValue()); // 별점 높은순
            } else {
                return review1.getRatingValue().compareTo(review2.getRatingValue()); // 별점 낮은순
            }
        };

        // 날짜 기준을 우선적으로 적용하고 같은 날짜 내에서만 별점 기준을 적용
        return reviews.stream()
                .sorted((review1, review2) -> {
                    // 날짜 비교
                    int dateComparison = dateComparator.compare(review1, review2);

                    // 같은 날짜라면 별점 비교
                    if (dateComparison == 0) {
                        return ratingComparator.compare(review1, review2);
                    }

                    // 날짜가 다르면 날짜 비교 결과 반환
                    return dateComparison;
                })
                .collect(Collectors.toList());
    }

    //영화 리뷰 페이지 GET
    String getMovieReview(Model model, Authentication auth, String searchText, Integer pageIdx, String dateSort, String ratingSort, String movieCd) {
        if(pageIdx == null) pageIdx = 1;
        if(dateSort == null) dateSort = "recent"; //결과물인 movieReviewDTOPage을 어떻게 정렬할지.
        if(ratingSort == null) ratingSort = "high";
        if(searchText == null) searchText = "";
        model.addAttribute("dateSort", dateSort);
        model.addAttribute("ratingSort", ratingSort);

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
//        //페이지네이션사용시 현재 컨트롤러를 다시 호출하므로 필요한 값.
//        model.addAttribute("movieCd", movieCd);
        Member member = memberObj.get();
        Long currentUserId = member.getId();

        //리뷰 목록을 보여줌 : 한페이지에 5개 보여주도록
        //검색어없으면 ""을 검색하므로 모든 결과가 가져올것
        //Page<MovieReview> movieReviewList = movieReviewRepository.findAllByMovieCdAndStatus(movieCd, PageRequest.of(pageIdx - 1, 5));
        Page<MovieReview> movieReviewList = movieReviewRepository.search(searchText, PageRequest.of(pageIdx - 1, 5));
        List<MovieReviewDTO> movieReviewDTOList = new ArrayList<>();
        for (MovieReview movieReview : movieReviewList) {
            //리뷰 총 좋아요 가져오기
            Long movieReviewId = movieReview.getId();
            List<MovieReviewLike> movieReviewLikeList = movieReviewLikeRepository.findAllByMovieReviewIdAndStatus(movieReviewId);
            int likeCount = movieReviewLikeList.size();
            //내가 좋아요를 눌렀는지 상태 가져오기
            Optional<MovieReviewLike> movieReviewLikeObj = movieReviewLikeRepository.findByUsernameAndMovieReviewId(username, movieReviewId);
            Boolean isLiked;
            if(movieReviewLikeObj.isPresent()){
                int status = movieReviewLikeObj.get().getStatus();
                isLiked = (status == 1); //상태 저장
            }else{
                isLiked = false; //좋아요 안누름 상태 저장
            }
            Long authorMemberId = movieReview.getMember().getId(); //review.html에서 유저 프로필로 link하기위해 필요
            MovieReviewDTO movieReviewDTO = MovieReviewDTO.builder()
                    .movieReviewId(movieReviewId)
                    .content(movieReview.getContent())
                    .ratingValue(movieReview.getRatingValue())
                    .movieNm(movieReview.getMovie().getMovieNm())
                    .moviePosterURL(movieReview.getMovie().getPosterURL())
                    .reviewUpdateDate(movieReview.getUpdateDate())
                    .likeCount(likeCount)
                    .authorNickname(movieReview.getMember().getNickname())
                    .authorProfileImage(movieReview.getMember().getProfileImage())
                    .authorMemberId(authorMemberId)
                    .isLiked(isLiked) //내가 좋아요를 눌렀는지 보냄
                    .isAuthor(currentUserId == authorMemberId) //영화 리뷰 작성자인지 보냄
                    .build();
            movieReviewDTOList.add(movieReviewDTO); //전체 DTO 리스트에 추가
        }

        //정렬 기준으로 정렬!
        movieReviewDTOList = sortReviews(movieReviewDTOList, dateSort, ratingSort);

        //페이지로 전달
        Page<MovieReviewDTO> movieReviewDTOPage = new PageImpl<>(
                movieReviewDTOList,
                movieReviewList.getPageable(),
                movieReviewList.getTotalElements()
        );

        model.addAttribute("movieReviewDTOPage",movieReviewDTOPage);

        return "review/review";
    }

    //영화 리뷰 작성 내용을 서버에 저장
    @Transactional
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

    //영화 리뷰 삭제 요청
    @Transactional
    ResponseEntity<String> deleteMovieReview(Long reviewId, Authentication auth) {
        Optional<MovieReview> movieReviewObj = movieReviewRepository.findById(reviewId);
        if(!movieReviewObj.isPresent())
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieReview/delete", "영화 리뷰를 찾을 수 없습니다.", ResponseEntity.class);

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieReview/delete", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        MovieReview movieReview = movieReviewObj.get();
        Member member = memberObj.get();
        
        if(member.getId() != movieReview.getMember().getId())
            return ErrorService.send(HttpStatus.NOT_ACCEPTABLE.value(), "/api/movieReview/delete", "본인이 작성한 리뷰가 아닙니다.", ResponseEntity.class);

        movieReviewRepository.delete(movieReview); //삭제 진행

        return ResponseEntity.noContent().build();
    }

}
