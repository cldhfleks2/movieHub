package com.cldhfleks2.moviehub.like;

import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.community.PostRepository;
import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.movie.MovieLike;
import com.cldhfleks2.moviehub.like.movie.MovieLikeRepository;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLike;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLikeRepository;
import com.cldhfleks2.moviehub.like.post.PostLike;
import com.cldhfleks2.moviehub.like.post.PostLikeRepository;
import com.cldhfleks2.moviehub.like.postreview.PostReviewLike;
import com.cldhfleks2.moviehub.like.postreview.PostReviewLikeRepository;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.member.MemberService;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.moviereview.MovieReview;
import com.cldhfleks2.moviehub.moviereview.MovieReviewRepository;
import com.cldhfleks2.moviehub.notification.Notification;
import com.cldhfleks2.moviehub.notification.NotificationRepository;
import com.cldhfleks2.moviehub.notification.NotificationTargetType;
import com.cldhfleks2.moviehub.notification.NotificationType;
import com.cldhfleks2.moviehub.postreview.PostReview;
import com.cldhfleks2.moviehub.postreview.PostReviewRepository;
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
    private final NotificationRepository notificationRepository;
    private final MovieReviewLikeRepository movieReviewLikeRepository;
    private final MovieReviewRepository movieReviewRepository;

    //영화 리뷰 좋아요 요청
    @Transactional
    ResponseEntity<String> addMovieReviewLike(Long reviewId, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieReview/like", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<MovieReview> movieReviewObj = movieReviewRepository.findById(reviewId);
        if(!movieReviewObj.isPresent()) //리뷰 정보 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieReview/like", "영화 리뷰를 찾을 수 없습니다.", ResponseEntity.class);

        //리뷰 작성자는 좋아요 요청 불가
        Long currentUserId = memberObj.get().getId();
        Long movieReviewAuthorId = movieReviewObj.get().getMember().getId();
        if(currentUserId == movieReviewAuthorId)
            return ErrorService.send(HttpStatus.FORBIDDEN.value(), "/api/movieReview/like", "본인의 리뷰는 좋아요를 요청할 수 없습니다.", ResponseEntity.class);

        //이미 좋아요를 누른상태인지
        Optional<MovieReviewLike> movieReviewLikeObj = movieReviewLikeRepository.findByUsernameAndMovieReviewId(username, reviewId);
        boolean pushNotification = false; //알림을 보낼건지
        if(!movieReviewLikeObj.isPresent()){ //처음 누를때
            Member member = memberObj.get();
            MovieReview movieReview = movieReviewObj.get();
            MovieReviewLike movieReviewLike = new MovieReviewLike();
            movieReviewLike.setMember(member);
            movieReviewLike.setMovieReview(movieReview);
            movieReviewLikeRepository.save(movieReviewLike); //리뷰 좋아요 저장
            pushNotification = true;
        }else{
            MovieReviewLike movieReviewLike = movieReviewLikeObj.get();
            int status = movieReviewLike.getStatus();
            status = (status + 1) % 2; //상태 toggle
            movieReviewLike.setStatus(status);
            movieReviewLikeRepository.save(movieReviewLike); //상태를 바꿔서 저장
            pushNotification = (status == 1);
        }

        //알림 보내는 로직 : 회원인 유저에게만 보냄
        if(pushNotification){
            MovieReview movieReview = movieReviewObj.get();
            Long receiverId = movieReview.getMember().getId();
            Optional<Member> receiverObj = memberRepository.findByIdAndStatus(receiverId); //회원인 유저만 가져옴
            if(receiverObj.isPresent()){ //보낼 사람이 존재할때만 ㅇㅇ
                Member receiver = receiverObj.get();
                Member sender = memberObj.get();
                String nickname = sender.getNickname();
                String message = nickname + "님이 리뷰에 좋아요를 눌렀습니다.";
                Notification notification = Notification.create()
                        .receiver(receiver)
                        .sender(sender)
                        .notificationType(NotificationType.LIKE_RECEIVED)
                        .targetType(NotificationTargetType.REVIEW)
                        .targetId(reviewId) //이 리뷰에서 이벤트가 일어남
                        .message(message)
                        .build();
                notificationRepository.save(notification); //DB 저장
            }
        }



        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //영화 좋아요 요청
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
        if(postReview.getMember().getId() == member.getId()) //본인 댓글일때
            return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/post/review/like", "본인 댓글은 좋아요를 할 수 없습니다.", ResponseEntity.class);

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
        String nickname = member.getNickname();
        String message = nickname + "님이 댓글에 좋아요를 눌렀습니다.";
        Notification notification = Notification.create()
                .receiver(postReview.getMember())
                .sender(member)
                .notificationType(NotificationType.LIKE_RECEIVED)
                .targetType(NotificationTargetType.COMMUNITY_REVIEW)
                .targetId(postReview.getPost().getId())
                .message(message)
                .build();
        notificationRepository.save(notification); //save

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
        if(post.getMember().getId() == member.getId()) //본인 게시글 좋아요는 불가능
            return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/post/like", "본인 게시글은 좋아요 할 수 없습니다.", ResponseEntity.class);

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
        String nickname = member.getNickname();
        String message = nickname + "님이 게시글에 좋아요를 눌렀습니다.";
        Notification notification = Notification.create()
                .receiver(post.getMember())
                .sender(member)
                .notificationType(NotificationType.LIKE_RECEIVED)
                .targetType(NotificationTargetType.COMMUNITY_POST)
                .targetId(post.getId())
                .message(message)
                .build();
        notificationRepository.save(notification); //save

        return ResponseEntity.ok().build();
    }

}
