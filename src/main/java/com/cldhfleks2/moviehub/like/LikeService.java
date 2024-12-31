package com.cldhfleks2.moviehub.like;

import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.community.PostRepository;
import com.cldhfleks2.moviehub.community.PostReview;
import com.cldhfleks2.moviehub.community.PostReviewRepository;
import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.member.MemberService;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final MemberService memberService;
    private final PostReviewRepository postReviewRepository;
    private final PostReviewLikeRepository postReviewLikeRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

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
        MovieDTO movieDetail = MovieDTO.create()
                .movieCd(movieCd)
                .build();
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

    //댓글 좋아요 요청 : save or status toggle
    @Transactional
    ResponseEntity<String> likePostReview(Long reviewId, Authentication auth){
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/review/like", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<PostReview> postReviewObj = postReviewRepository.findById(reviewId);
        if(!postReviewObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/review/like", "댓글을 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        PostReview postReview = postReviewObj.get();
        if(postReview.getMember().getId() == member.getId())
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/review/like", "본인 댓글은 좋아요를 할 수 없습니다.", ResponseEntity.class);

        Optional<PostReviewLike> postReviewLikeObj = postReviewLikeRepository.findByReviewIdAndMemberId(reviewId, member.getId());
        if(postReviewLikeObj.isPresent()){ //기존에 좋아요 요청이 있었을때는 : status toggle
            PostReviewLike postReviewLike = postReviewLikeObj.get();
            int status = postReviewLike.getStatus();
            status = (status + 1) % 2; //toggle
            postReviewLike.setStatus(status);
            postReviewLikeRepository.save(postReviewLike); // update
        }else{
            PostReviewLike postReviewLike = PostReviewLike.create()
                    .review(postReview)
                    .sender(member)
                    .build();
            postReviewLikeRepository.save(postReviewLike); //DB 저장
        }

        //알림 보내기

        return ResponseEntity.ok().build();
    }

    //게시글 좋아요 요청 : save or status toggle
    @Transactional
    ResponseEntity<String> likePost(Long postId, Authentication auth){
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/like", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/like", "게시글 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Post post = postObj.get();
        Optional<PostLike> postLikeObj = postLikeRepository.findByPostIdAndMemberId(postId, member.getId());
        if(postLikeObj.isPresent()){
            PostLike postLike = postLikeObj.get();
            int status = postLike.getStatus();
            status = (status + 1) % 2; //상태 toggle
            postLike.setStatus(status);
            postLikeRepository.save(postLike); //update
        }else{
            PostLike postLike = PostLike.create()
                    .post(post)
                    .sender(member)
                    .build();
            postLikeRepository.save(postLike); //save
        }

        //알림 보내기

        return ResponseEntity.ok().build();
    }





}
