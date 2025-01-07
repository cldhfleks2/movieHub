package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.community.CommunityService;
import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.community.PostDTO;
import com.cldhfleks2.moviehub.community.PostRepository;
import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLike;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLikeRepository;
import com.cldhfleks2.moviehub.like.post.PostLike;
import com.cldhfleks2.moviehub.like.post.PostLikeRepository;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.movie.MovieService;
import com.cldhfleks2.moviehub.movie.actor.MovieActor;
import com.cldhfleks2.moviehub.movie.actor.MovieActorRepository;
import com.cldhfleks2.moviehub.movie.audit.MovieAudit;
import com.cldhfleks2.moviehub.movie.audit.MovieAuditRepository;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStatRepository;
import com.cldhfleks2.moviehub.movie.director.MovieDirector;
import com.cldhfleks2.moviehub.movie.director.MovieDirectorRepository;
import com.cldhfleks2.moviehub.movie.genre.MovieGenre;
import com.cldhfleks2.moviehub.movie.genre.MovieGenreRepository;
import com.cldhfleks2.moviehub.moviereview.MovieReview;
import com.cldhfleks2.moviehub.moviereview.MovieReviewDTO;
import com.cldhfleks2.moviehub.moviereview.MovieReviewRepository;
import com.cldhfleks2.moviehub.postreview.PostReview;
import com.cldhfleks2.moviehub.postreview.PostReviewDTO;
import com.cldhfleks2.moviehub.postreview.PostReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final MovieRepository movieRepository;
    private final MovieService movieService;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieAuditRepository movieAuditRepository;
    private final MovieDirectorRepository movieDirectorRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieDailyStatRepository movieDailyStatRepository;
    private final MovieReviewRepository movieReviewRepository;
    private final MovieReviewLikeRepository movieReviewLikeRepository;
    private final PostRepository postRepository;
    private final PostReviewRepository postReviewRepository;
    private final MemberRepository memberRepository;
    private final CommunityService communityService;
    private final PostLikeRepository postLikeRepository;

    @Value("${file.dir}")
    private String fileDir;

    @Value(("${file.dir.on.db}"))
    private String fileDirOnDb;

    //영화 관리자 페이지 GET
    String getManagerMovie() {
        return "manager/movie";
    }

    //영화 관리자 페이지 : 검색 결과 뷰 GET
    String searchMovie(Model model, Integer pageIdx, String keyword){
        if(pageIdx == null) pageIdx = 1;
        if(keyword == null) keyword = "";

        int pageSize = 5;
        Page<Movie> searchMoviePage = movieRepository.findByMovieNmAndStatus(keyword, PageRequest.of(pageIdx-1, pageSize));
        model.addAttribute("searchMoviePage", searchMoviePage);

        model.addAttribute("totalMovieCount", searchMoviePage.getTotalElements());
        return "manager/movie :: #searchResultSection";
    }

    //영화 관리자 페이지 : 영화 상세 정보 뷰 GET
    String getMovieDTO(Model model, Long movieId){
        MovieDTO movieDTO = movieService.getMovieDTO(movieId);
        movieDTO.setMovieId(movieId);
        model.addAttribute("movie", movieDTO);
        return "manager/movie :: #movieContent";
    }

    //날짜 가져오는 함수
    private String getCurrentDay() {
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter);
    }

    //영화 관리자 페이지 : 영화 정보 수정 요청
    @Transactional
    ResponseEntity<String> editMovie(MovieDTO movieDTO) {
        Long movieId = movieDTO.getMovieId();
        Optional<Movie> movieObj = movieRepository.findById(movieId);
        if(!movieObj.isPresent()) //영화 존재 여부 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/manager/movie/edit", "영화 정보를 찾을 수 없습니다.", ResponseEntity.class);
        Movie movie = movieObj.get();

        //기존 객체 삭제하고 새로 추가 : MovieGenre, MovieAudit, MovieDirector, MovieActor
        for(MovieGenre movieGenre : movieDTO.getGenreList()){
            movieGenre.setMovie(movie);
            movieGenreRepository.save(movieGenre);
        }
        for(MovieAudit movieAudit : movieDTO.getAuditList()){
            movieAudit.setMovie(movie);
            movieAuditRepository.save(movieAudit);
        }
        for (MovieDirector movieDirector : movieDTO.getDirectorList()){
            movieDirector.setMovie(movie);
            movieDirectorRepository.save(movieDirector);
        }
        for (MovieActor movieActor : movieDTO.getActorList()){
            movieActor.setMovie(movie);
            movieActorRepository.save(movieActor);
        }
        
        //MovieDailyStat, Movie는 수정만 진행
        String day = getCurrentDay();
        Optional<MovieDailyStat> movieDailyStatObj = movieDailyStatRepository.findByDayAndMovieIdAndStatus(day, movieId); //오늘날짜 찾기
        MovieDailyStat movieDailyStat;
        if(movieDailyStatObj.isPresent()){
            //오늘 날짜 있으면 수정
            movieDailyStat = movieDailyStatObj.get();
        }else{
            //오늘 날짜 없으면 전체에서 검색
            List<MovieDailyStat> movieDailyStatList = movieDailyStatRepository.findByMovieIdAndStatus(movieId);
            if(!movieDailyStatList.isEmpty()){
                //전체 날짜 에서 있으면 가장 최근것
                movieDailyStat = movieDailyStatList.get(movieDailyStatList.size()-1);
            }else{
                //전체 날짜에서도 없으면 새로 만들자
                movieDailyStat = new MovieDailyStat();
            }
        }
        movieDailyStat.setAudiCnt(movieDTO.getAudiCnt());
        movieDailyStat.setMovie(movie);
        movieDailyStatRepository.save(movieDailyStat); //수정

        //movie 수정
        movie.setMovieCd(movieDTO.getMovieCd());
        movie.setMovieNm(movieDTO.getMovieNm());
        movie.setMovieNmEn(movieDTO.getMovieNmEn());
        movie.setShowTm(movieDTO.getShowTm());
        movie.setOpenDt(movieDTO.getOpenDt());
        movie.setAudiAcc(movieDTO.getAudiAcc());
        if(movieDTO.getPosterURL() != null) movie.setPosterURL(movieDTO.getPosterURL());
        movie.setPrdtYear(movieDTO.getPrdtYear());
        movie.setTypeNm(movieDTO.getTypeNm());
        movie.setSalesAcc(movieDTO.getSalesAcc());
        movieRepository.save(movie); //수정

        return ResponseEntity.noContent().build();
    }

    //영화 관리자 페이지 : 영화 포스터 수정 요청
    @Transactional
    ResponseEntity<String> editMoviePoster(MultipartFile posterImg, Long movieId) throws Exception{
        Optional<Movie> movieObj = movieRepository.findById(movieId);
        if(!movieObj.isPresent())
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/manager/movie/edit/img", "영화 정보를 찾을 수 없습니다.", ResponseEntity.class);
        Movie movie = movieObj.get();

        //현재 프로젝트 경로를 가져와서 파일을 저장할 경로를 더함
        String uploadDir = System.getProperty("user.dir") + fileDir;

        //저장할 파일이름과 확장명을 구함
        String uuid = UUID.randomUUID().toString();
        String ext = posterImg.getOriginalFilename().substring(posterImg.getOriginalFilename().lastIndexOf("."));
        //실제 저장할 위치에 폴더가 없으면 생성
        File dir = new File(uploadDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        String fileName = uuid + ext; //새로 생성된 파일이름
        String filePath = uploadDir + fileName; //실제 이미지가 저장될 경로
        posterImg.transferTo(new File(filePath));  //실제 이미지 저장

        //DB에 저장될 URL 경로 : src값에 사용하면 바로 이미지를 띄울 수 있는 경로
        String filePathOnDB = fileDirOnDb + fileName; // /image/ + uuid + ext
        movie.setPosterURL(filePathOnDB);
        movieRepository.save(movie); //수정

        return ResponseEntity.noContent().build();
    }

    //영화 리뷰 관리자 페이지 GET
    String getMovieReview() {
        return "manager/movieReview";
    }

    //영화 리뷰 관리자 페이지 리뷰 검색
    String searchMovieReview(Model model, Integer pageIdx, String keyword){
        if(pageIdx == null) pageIdx = 1;
        if(keyword == null) keyword = "";

        int pageSize = 10; //한페이지에 보여줄 갯수
        Page<MovieReview> movieReviewPage = movieReviewRepository.searchByContentAndMovieNmAndMovieNmEn(keyword, PageRequest.of(pageIdx-1, pageSize));
        List<MovieReviewDTO> searchList = new ArrayList<>();
        for (MovieReview movieReview : movieReviewPage.getContent()) {
            MovieReviewDTO movieReviewDTO = MovieReviewDTO.builder()
                    .movieNm(movieReview.getMovie().getMovieNm())
                    .content(movieReview.getContent())
                    .ratingValue(movieReview.getRatingValue())
                    .member(movieReview.getMember())
                    .reviewUpdateDate(movieReview.getUpdateDate())
                    .id(movieReview.getId())
                    .build();
            searchList.add(movieReviewDTO);
        }

        //페이지로 전달
        Page<MovieReviewDTO> searchPage = new PageImpl<>(
                searchList,
                movieReviewPage.getPageable(),
                movieReviewPage.getTotalElements()
        );

        model.addAttribute("searchPage", searchPage);

        return "manager/movieReview :: #reviewList";
    }

    //영화 리뷰 관리자 페이지 : 영화 리뷰 상세 검색
    String getMovieReviewDetail(Model model, Long reviewId){
        Optional<MovieReview> movieReviewObj = movieReviewRepository.findById(reviewId);
        if(!movieReviewObj.isPresent())
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/manager/movieReview/detail", "영화 리뷰를 찾을 수 없습니다.", String.class);

        MovieReview movieReview = movieReviewObj.get();
        //리뷰의 좋아요 갯수를 구함
        List<MovieReviewLike> movieReviewLikeList = movieReviewLikeRepository.findAllByMovieReviewIdAndStatus(reviewId);
        int likeCount = movieReviewLikeList.size();
        MovieReviewDTO movieReviewDTO = MovieReviewDTO.builder()
                .movie(movieReview.getMovie())
                .member(movieReview.getMember())
                .content(movieReview.getContent())
                .likeCount(likeCount)
                .ratingValue(movieReview.getRatingValue())
                .id(reviewId)
                .build();
        model.addAttribute("reviewDTO", movieReviewDTO);

        return "manager/movieReview :: #reviewDetailSection";
    }

    //영화 리뷰 관리자 페이지 : 영화 리뷰 삭제
    @Transactional
    ResponseEntity<String> deleteMovieReview(Long reviewId){
        Optional<MovieReview> movieReviewObj = movieReviewRepository.findById(reviewId);
        if(!movieReviewObj.isPresent())
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/manager/movieReview/delete", "영화 리뷰를 찾을 수 없습니다.", ResponseEntity.class);

        movieReviewRepository.deleteById(reviewId); //영화 리뷰 삭제
        
        return ResponseEntity.noContent().build();
    }

    //영화 리뷰 관리자 페이지 : 영화 리뷰 수정
    @Transactional
    ResponseEntity<String> editMovieReview(Long reviewId, String content) {
        Optional<MovieReview> movieReviewObj = movieReviewRepository.findById(reviewId);
        if(!movieReviewObj.isPresent())
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/manager/movieReview/edit", "영화 리뷰를 찾을 수 없습니다.", ResponseEntity.class);
        MovieReview movieReview = movieReviewObj.get();
        movieReview.setContent(content); //내용 수정
        movieReviewRepository.save(movieReview); //수정

        return ResponseEntity.noContent().build();
    }

    //게시글 관리 페이지 GET
    String getPost() {
        return "manager/post";
    }

    //게시글 관리 페이지 : 게시글 검색
    String searchPost(Model model, Integer pageIdx, String keyword){
        if (pageIdx == null) pageIdx = 1;
        if(keyword == null) keyword = "";

        int pageSize = 10; //10개 가져올거
        Page<Post> postPage = postRepository.findAllByKeywordAndStatus(keyword, PageRequest.of(pageIdx-1, pageSize));
        List<PostDTO> searchList = new ArrayList<>();
        for (Post post : postPage.getContent()) {
            Long postId = post.getId();
            List<PostReview> postReviewList = postReviewRepository.findAllByPostIdAndStatus(postId);
            Long reviewCount = (long) postReviewList.size();
            PostDTO postDTO =  PostDTO.create()
                    .postType(post.getPostType())
                    .title(post.getTitle())
                    .reviewCount(reviewCount)
                    .member(post.getMember())
                    .updateDate(post.getUpdateDate())
                    .view(post.getView())
                    .postId(postId)
                    .build();
            searchList.add(postDTO);
        }

        //페이지로 전달
        Page<PostDTO> postDTOPage = new PageImpl<>(
                searchList,
                postPage.getPageable(),
                postPage.getTotalElements()
        );

        model.addAttribute("postDTOPage", postDTOPage);

        return "manager/post :: #postContainer";
    }

    //게시글 관리 페이지 : 게시글 상세 검색
    String searchPostDetail(Model model, Authentication auth, Long postId) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "", "게시글 정보를 찾을 수 없습니다.", String.class);
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "", "유저 정보를 찾을 수 없습니다.", String.class);

        //게시글 뷰
        Post post = postObj.get();
        List<PostReview> postReviewList = postReviewRepository.findAllByPostIdAndStatus(postId);
        Long reviewCount = (long) postReviewList.size();
        List<PostLike> postLikeList = postLikeRepository.findAllByPostIdAndStatus(postId);
        Long likeCount = (long) postLikeList.size();
        PostDTO postDTO = PostDTO.create()
                .member(memberObj.get())
                .updateDate(post.getUpdateDate())
                .postId(postId)
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .view(post.getView())
                .reviewCount(reviewCount)
                .likeCount(likeCount)
                .build();
        model.addAttribute("postDTO", postDTO);

        //댓글 뷰
        List<PostReview> allReviews = postReviewRepository.findAllByPostIdAndStatus(postId);
        Member member = memberObj.get();
        List<PostReviewDTO> parentReviews = allReviews.stream()// 부모 댓글만 필터링 (parent가 null인 경우)
                .filter(review -> review.getParent() == null)
                .map(review -> communityService.convertToPostReviewDTO(review, allReviews, member.getId()))
                .collect(Collectors.toList());

        model.addAttribute("postReviewDTOList", parentReviews);
        model.addAttribute("postReviewCnt", allReviews.size());

        //게시글 아이디 보내줌
        model.addAttribute("postId", postId);

        return "manager/post :: #postDetailSection";
    }

    //게시글 관리 페이지 : 게시글 수정 요청
    @Transactional
    ResponseEntity<String> editPost(PostDTO postDTO) {
        Optional<Post> postObj = postRepository.findById(postDTO.getPostId());
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/manager/post/edit", "게시글 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Post post = postObj.get();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setPostType(postDTO.getPostType());
        postRepository.save(post); //update
        
        return ResponseEntity.noContent().build(); //204 전달
    }

    //게시글 관리 페이지 : 게시글 삭제 요청
    @Transactional
    ResponseEntity<String> deletePost(Long postId) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/manager/post/delete", "게시글 정보를 찾을 수 없습니다.", ResponseEntity.class);
        Post post = postObj.get();
        postRepository.delete(post); //soft delete

        return ResponseEntity.noContent().build(); //204 전달
    }

}
