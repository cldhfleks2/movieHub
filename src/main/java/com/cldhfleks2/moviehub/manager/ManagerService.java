package com.cldhfleks2.moviehub.manager;

import com.cldhfleks2.moviehub.error.ErrorService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Value("${file.dir}")
    private String fileDir;

    @Value(("${file.dir.on.db}"))
    private String fileDirOnDb;

    //영화 관리자 페이지 GET
    String getManagerMovie(Authentication authentication, Model model) {
        return "manager/movie";
    }

    //영화 검색 결과 뷰 GET
    String searchMovie(Model model, Integer pageIdx, String keyword){
        if(pageIdx == null) pageIdx = 1;
        if(keyword == null) keyword = "";

        int pageSize = 10;
        Page<Movie> searchMoviePage = movieRepository.findByMovieNmAndStatus(keyword, PageRequest.of(pageIdx-1, pageSize));

        model.addAttribute("searchMovieList", searchMoviePage);
        return "manager/movie :: #searchResultSection";
    }

    //movieContent 뷰 GET
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

    //영화 정보 수정 요청
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

    //영화 포스터 수정 요청
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
    String getMovieReview(Authentication auth, Model model) {
        return "manager/movieReview";
    }
}
