package com.cldhfleks2.moviehub.report;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import com.cldhfleks2.moviehub.review.MovieReview;
import com.cldhfleks2.moviehub.review.MovieReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final MemberRepository memberRepository;
    private final MovieReviewRepository movieReviewRepository;
    private final MovieReviewReportRepository movieReviewReportRepository;
    private final MovieRepository movieRepository;
    private final MovieReportRepository memberReportRepository;

    //리뷰 신고 요청
    ResponseEntity<String> saveMovieReviewReport(MovieReviewReportDTO movieReviewReportDTO, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movieReview/report", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        if(movieReviewReportDTO == null) //신고 내용이 전달 안된경우
            return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/movieReview/report", "신고 정보가 전달 되지 않았습니다.", ResponseEntity.class);

        Long reviewId = movieReviewReportDTO.getMovieReviewId();
        if(reviewId == null) //신고할 리뷰 id가 전달 안된경우
            return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/movieReview/report", "신고 리뷰 id를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<MovieReview> movieReviewObj = movieReviewRepository.findById(reviewId);
        if(!movieReviewObj.isPresent()) //리뷰 정보 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movieReview/report", "영화 리뷰를 찾을 수 없습니다.", ResponseEntity.class);

        MovieReview movieReview = movieReviewObj.get();
        Member member = memberObj.get();
        MovieReviewReport movieReviewReport = new MovieReviewReport();
        movieReviewReport.setMember(member);
        movieReviewReport.setMovieReview(movieReview);
        movieReviewReport.setSPOILER(movieReviewReportDTO.getSPOILER());
        movieReviewReport.setWRONG(movieReviewReportDTO.getWRONG());
        movieReviewReport.setUNRELATED(movieReviewReportDTO.getUNRELATED());
        movieReviewReport.setHARMFUL(movieReviewReportDTO.getHARMFUL());
        movieReviewReport.setHATE(movieReviewReportDTO.getHATE());
        movieReviewReport.setCOPYRIGHT(movieReviewReportDTO.getCOPYRIGHT());
        movieReviewReport.setSPAM(movieReviewReportDTO.getSPAM());
        movieReviewReport.setReportDetail(movieReviewReportDTO.getReportDetail());
        movieReviewReportRepository.save(movieReviewReport); //신고내용 DB저장

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //영화 신고 요청
    ResponseEntity<String> saveMovieReport(MovieReportDTO movieReportDTO, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/movie/report", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        if(movieReportDTO == null) //신고 내용이 전달 안된경우
            return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/movie/report", "신고 정보가 전달 되지 않았습니다.", ResponseEntity.class);

        String movieCd = movieReportDTO.getMovieCd();
        if(movieCd == null) //신고할 리뷰 id가 전달 안된경우
            return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/movie/report", "신고할 영화 고유값이 전달 되지 않았습니다.", ResponseEntity.class);

        Optional<Movie> movieObj = movieRepository.findByMovieCd(movieCd);
        if(!movieObj.isPresent()) //리뷰 정보 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/movie/report", "신고할 영화를 찾을 수 없습니다.", ResponseEntity.class);

        Movie movie = movieObj.get();
        Member member = memberObj.get();
        MovieReport movieReport = new MovieReport();
        movieReport.setMember(member);
        movieReport.setMovie(movie);
        movieReport.setPOSTER(movieReportDTO.getPOSTER());
        movieReport.setMOVIENAME(movieReportDTO.getMOVIENAME());
        movieReport.setMOVIEPEOPLE(movieReportDTO.getMOVIEPEOPLE());
        movieReport.setHARMFUL(movieReportDTO.getHARMFUL());
        movieReport.setHATE(movieReportDTO.getHATE());
        movieReport.setReportDetail(movieReportDTO.getReportDetail());
        memberReportRepository.save(movieReport); //신고내용 DB저장

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
