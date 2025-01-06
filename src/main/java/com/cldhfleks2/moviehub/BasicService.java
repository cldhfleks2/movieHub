package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.api.KOBISRequestService;
import com.cldhfleks2.moviehub.bookmark.BookMark;
import com.cldhfleks2.moviehub.bookmark.BookMarkRepository;
import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.community.PostDTO;
import com.cldhfleks2.moviehub.community.PostRepository;
import com.cldhfleks2.moviehub.like.movie.MovieLike;
import com.cldhfleks2.moviehub.like.movie.MovieLikeRepository;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLike;
import com.cldhfleks2.moviehub.like.moviereview.MovieReviewLikeRepository;
import com.cldhfleks2.moviehub.like.post.PostLikeRepository;
import com.cldhfleks2.moviehub.movie.*;
import com.cldhfleks2.moviehub.moviereview.MovieReview;
import com.cldhfleks2.moviehub.moviereview.MovieReviewDTO;
import com.cldhfleks2.moviehub.moviereview.MovieReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicService {
    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final KOBISRequestService kobisRequestService;
    private final MovieLikeRepository movieLikeRepository;
    private final BookMarkRepository bookMarkRepository;
    private final MovieReviewRepository movieReviewRepository;
    private final MovieReviewLikeRepository movieReviewLikeRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    //헤더 페이지 GET
    String getHeader(){
        return "header/header :: #header";
    }

    //날짜 가져오는 함수
    private String getCurrentDay() {
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDate.format(formatter);
    }

    //검색결과 정렬 담당 함수 : getSearch에서 사용
    public List<MovieDTO> sortMovieList(List<MovieDTO> movieDTOList, String keyword, String sortBy) {
        if(movieDTOList == null || movieDTOList.isEmpty()) //유효한 리스트인지 확인
            return movieDTOList;

        // 기본 Comparator 생성 (전체 정렬 조건은 없으므로 기본값은 null 처리)
        Comparator<MovieDTO> comparator = null;

        // 1. 관련도순 (relevance) 정렬
        if (sortBy.equals("relevance")) {
            comparator = (movie1, movie2) -> {
                boolean movie1Matches = movie1.getMovieNm().contains(keyword);
                boolean movie2Matches = movie2.getMovieNm().contains(keyword);
                return Boolean.compare(movie2Matches, movie1Matches); // 매칭된 영화가 앞에 오도록
            };
        }
        // 2. 개봉일순 (date) 정렬
        else if (sortBy.equals("date")) {
            comparator = (movie1, movie2) -> {
                String openDt1 = movie1.getOpenDt();
                String openDt2 = movie2.getOpenDt();
                return openDt2.compareTo(openDt1); // 최신 개봉일이 먼저 오도록
            };
        }
        // 3. 평점순 (rating) 정렬 : not-found값은 최하위 우선순위로 지정
        else if (sortBy.equals("rating")) {
            comparator = (movie1, movie2) -> {
                // "not-found"인 경우 0으로 처리하여 마지막으로 정렬
                String rating1 = movie1.getRating();
                String rating2 = movie2.getRating();

                if ("not-found".equals(rating1) && !"not-found".equals(rating2)) {
                    return 1; // movie1을 마지막으로 배치
                } else if (!"not-found".equals(rating1) && "not-found".equals(rating2)) {
                    return -1; // movie2를 마지막으로 배치
                } else if ("not-found".equals(rating1) && "not-found".equals(rating2)) {
                    return 0; // 둘 다 "not-found"일 경우 순서 유지
                } else {
                    // 정상적인 평점 값 비교
                    double rt1 = Double.parseDouble(rating1);
                    double rt2 = Double.parseDouble(rating2);
                    return Double.compare(rt2, rt1); // 높은 평점이 먼저 오도록
                }
            };
        }

        // 조건에 맞는 정렬 기준이 있는 경우에만 적용
        if (comparator != null) {
            return movieDTOList.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return movieDTOList; // 정렬 기준이 안맞으면 원래 그대로
    }

    //메인 페이지 GET
    String getMain(Model model){
        //1. 전체 일일 박스 오피스 DTO
        List<MovieDTO> totalTodayBoxOfficeMovie = movieService.getTotalTodayBoxOfficeMovie();
        model.addAttribute("totalTodayBoxOfficeMovie", totalTodayBoxOfficeMovie);

        //2. 전체 주간 박스오피스
        List<MovieDTO> totalWeeklyBoxOfficeMovie = movieService.getTotalWeeklyBoxOfficeMovie();
        model.addAttribute("totalWeeklyBoxOfficeMovie", totalWeeklyBoxOfficeMovie);

        //3. 주간 한국 박스오피스
        List<MovieDTO> koreaWeeklyBoxOfficeMovie = movieService.getKoreaWeeklyBoxOfficeMovie();
        model.addAttribute("koreaWeeklyBoxOfficeMovie", koreaWeeklyBoxOfficeMovie);

        //4. 주간 외국 박스오피스
        List<MovieDTO> foreignWeeklyBoxOfficeMovie = movieService.getForeignWeeklyBoxOfficeMovie();
        model.addAttribute("foreignWeeklyBoxOfficeMovie", foreignWeeklyBoxOfficeMovie);

        //5. 실시간 인기 리뷰 : 5개 가져옴
        int popularReviewCount = 5;
        PageRequest pageRequest = PageRequest.of(0, popularReviewCount);
        Page<MovieReview> popularReviewPage =  movieReviewRepository.findTopMovieReviewsByLikeCount(pageRequest);
        //DTO 생성
        List<MovieReviewDTO> movieReviewDTOList = new ArrayList<>();
        for(MovieReview movieReview : popularReviewPage.getContent()){ //보낼것들만 DTO에 담음
            Long movieReviewId = movieReview.getId();
            List<MovieReviewLike> movieReviewLikeList = movieReviewLikeRepository.findAllByMovieReviewIdAndStatus(movieReviewId);
            int likeCount = movieReviewLikeList.size();
            MovieReviewDTO movieReviewDTO = MovieReviewDTO.builder()
                    .member(movieReview.getMember())
                    .reviewUpdateDate(movieReview.getUpdateDate())
                    .movieNm(movieReview.getMovie().getMovieNm())
                    .content(movieReview.getContent())
                    .likeCount(likeCount)
                    .build();
            movieReviewDTOList.add(movieReviewDTO);
        }
        //페이지로 전달
        Page<MovieReviewDTO> popularReviewDTOPage = new PageImpl<>(
                movieReviewDTOList,
                popularReviewPage.getPageable(),
                popularReviewPage.getTotalElements()
        );
        model.addAttribute("popularReviewPage", popularReviewDTOPage);
        
        //6. 인기 게시글 : 10개만
        int popularPostCount = 5;
        pageRequest = PageRequest.of(0, popularPostCount);
        Page<Post> popularPostPage = postRepository.findTopPostsByLikeCount(pageRequest);
        //DTO생성
        List<PostDTO> popularPostDTOList = new ArrayList<>();
        for(Post post : popularPostPage.getContent()){
            PostDTO postDTO = PostDTO.create()
                    .postId(post.getId())
                    .postType(post.getPostType())
                    .updateDate(post.getUpdateDate())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .member(post.getMember())
                    .view(post.getView())
                    .build();
            popularPostDTOList.add(postDTO);
        }
        //페이지로 전달
        Page<PostDTO> popularPostDTOPage = new PageImpl<>(
                popularPostDTOList,
                popularPostPage.getPageable(),
                popularPostPage.getTotalElements()
        );
        model.addAttribute("popularPostPage", popularPostDTOPage);

        return "main/main";
    }

    //영화 상세 페이지 GET
    @Transactional
    String getDetail(String movieCd, Model model, Authentication auth, RedirectAttributes redirectAttributes) throws Exception{
        //DB에 없으면 DB에 추가하고 보여줌
        Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
        if(!movieObj.isPresent()){
            //KOBIS API 영화 상세 검색
            HttpResponse<String> movieDetailResponse =  kobisRequestService.sendMovieDetailRequest(movieCd);
            if(movieDetailResponse == null){
                log.error("getDetail movieDetailResponse 요청 실패!!!!");
                //에러 나서 메인페이지로 리다이렉트
                redirectAttributes.addFlashAttribute("alertMessage", "알 수 없는 에러가 발생했습니다.");
                return "redirect:/main";
            }
            String movieDetailResponseBody = movieDetailResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode movieDetailJsonNode = objectMapper.readTree(movieDetailResponseBody);
            JsonNode movieCdNode = movieDetailJsonNode.path("movieInfoResult").path("movieInfo").path("movieCd");
            if(movieCdNode.isNull()){
                //그래도 없으면 메인페이지로 리다이렉트
                redirectAttributes.addFlashAttribute("alertMessage", "영화가 존재하지 않습니다.");
                return "redirect:/main";
            }
            
            String currentDay = getCurrentDay();
            JsonNode movieInfo = movieDetailJsonNode.path("movieInfoResult").path("movieInfo");
            //영화를 DB에 저장
            ReturnEntitysDTO returnEntitysDTO = movieService.saveEntityAsMovieDetail(movieInfo , currentDay);
            if(returnEntitysDTO == null){
                //영화 포스터가 없으면 returnEntitysDTO가 null값으로 전달됨
                redirectAttributes.addFlashAttribute("alertMessage", "영화가 존재하지 않습니다.");
                return "redirect:/main";
            }

            //모델에 MovieDTO 담기
            MovieDTO movieDetail = movieService.convertToMovieDTO(returnEntitysDTO);
            model.addAttribute("movieDetail", movieDetail);
        }else{
            //DB에 있으면 그대로 영화 정보를 보여준다.
            Movie movie = movieObj.get();
            Long movieId = movie.getId();

            //MovieDailyStat은 아래 getMovieDTO에서 진행
            MovieDTO movieDetail = movieService.getMovieDTO(movieId);
            model.addAttribute("movieDetail", movieDetail);
        }

        //좋아요 상태 보내기
        String username = auth.getName();
        Boolean likeStatus;
        Optional<MovieLike> movieLikeObj = movieLikeRepository.findByUsernameAndMovieCd(username, movieCd);
        if(movieLikeObj.isPresent()){
            MovieLike movieLike = movieLikeObj.get();
            int status = movieLike.getStatus();
            likeStatus = (status == 1); //상태 저장
        }else{
            likeStatus = false;
        }
        model.addAttribute("likeStatus", likeStatus);

        //찜한 상태 보내기
        Boolean bookmarkStatus;
        Optional<BookMark> bookmarkObj = bookMarkRepository.findByUsernameAndMovieCd(username, movieCd);
        if(bookmarkObj.isPresent()){
            BookMark bookmark = bookmarkObj.get();
            int status = bookmark.getStatus();
            bookmarkStatus = (status == 1); //상태 저장
        }else{
            bookmarkStatus = false;
        }
        model.addAttribute("bookmarkStatus", bookmarkStatus);

        //영화의 좋아요 총 갯수
        List<MovieLike> movieLikeList = movieLikeRepository.findAllByMovieCdAndStatus(movieCd);
        int totalLikeCnt = (movieLikeList != null) ? movieLikeList.size() : 0;
        model.addAttribute("totalLikeCnt", totalLikeCnt);

        return "detail/detail";
    }

    //검색 페이지 GET
    String getSearch(String keyword, String category, String sortBy, Model model)  throws Exception{
        //기본값 지정
        if(keyword == null) keyword = ""; 
        if(category == null) category = ""; 
        if(sortBy == null) sortBy = "relevance"; //관련도 순

        if(keyword == "")
            return "search/search";

        
        //키워드로 검색 진행
        if(category.equals("movieName")){ //KOBIS API 사용
            log.info("영화제목으로 영화 목록 검색 시작");
            List<MovieDTO> movieList = movieService.searchMovieAsMovieName(keyword, 50); //영화 검색결과를 가져옴 갯수제한
            List<MovieDTO> sortedMovieList = sortMovieList(movieList, keyword ,sortBy);
            model.addAttribute("movieList", sortedMovieList); //정렬된 영화 목록을 전달
            log.info("영화제목으로 영화 목록을  갖고옴.");
            
        }else if(category.equals("moviePeople")){ //TMDB API 사용
            log.info("배우/감독명 검색 시작");
            List<PeopleDTO> peopleDTOList = movieService.searchProfileAsPeopleName(keyword); //프로필이미지만 가져옴
            model.addAttribute("peopleDTOList", peopleDTOList);
            log.info("배우/감독명으로 프로필들을 갖고옴.");
            
        }else if(category.equals("peopleClick")){
            log.info("해당 인물에 대한 영화 목록 검색 시작");
            //프로필을 클릭했을때 그 사람의 영화 목록을 줌
            //이때 키워드는 peopleId를 전달 해줌.
            Long peopleId = Long.parseLong(keyword);
            List<MovieDTO> movieList = movieService.searchMovieAsPeopleId(peopleId, 50); //검색결과 최대 8개 제한
            List<MovieDTO> sortedMovieList = sortMovieList(movieList, keyword ,sortBy);
            model.addAttribute("movieList", sortedMovieList); //정렬된 영화 목록을 전달
            log.info("해당 인물에 대한 영화 목록을 갖고옴.");
        }

        log.info("페이지 전송, category = {}", category);
        return "search/search";
    }

    //영화이름으로 해당 영화의 상세페이지로 이동 가능한지 결과를 리턴
    ResponseEntity<String> validateMovieByMovieNm(String movieNm, String openDt, Model model)  throws Exception{
        //movieNm에 해당하는 데이터가 KOBIS API에 있는지 먼저 확인
        HttpResponse<String> response = kobisRequestService.sendMovieListRequestByMovieNm(movieNm, openDt, openDt);
        //응답을 못받은 경우 에러
        if(response == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode movieListJsonNode = objectMapper.readTree(responseBody);
        //결과가 없으면 에러
        if(movieListJsonNode.isNull() || movieListJsonNode.path("movieListResult").path("totCnt").asInt() == 0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        //movieCd값 추출
        String movieCd = movieListJsonNode.path("movieListResult").path("movieList").get(0).path("movieCd").asText();

        //있으면 detail/detail 페이지 렌더링
        return ResponseEntity.status(HttpStatus.OK).body(movieCd);
    }

    //차트 페이지 GET
    String getChart(Model model){
        List<MovieDTO> movieDTOList = movieService.getMovieRankList();

        model.addAttribute("movieDTOList", movieDTOList);
        return "chart/chart";
    }

}
