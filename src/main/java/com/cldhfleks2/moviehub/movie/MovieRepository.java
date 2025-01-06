package com.cldhfleks2.moviehub.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    //movieCd로 Movie를 검색
    //status=0 인 것들도 가져온다.
    @Query("SELECT m FROM Movie m WHERE m.movieCd = :movieCd")
    Optional<Movie> findByMovieCd(String movieCd);

    //movieCd로 Movie를 검색
    @Query("SELECT m FROM Movie m WHERE m.movieCd = :movieCd AND m.status = 1")
    Optional<Movie> findByMovieCdAndStatus(String movieCd);

    //keyword로 movieNm을 검색
    @Query("SELECT m FROM Movie m " +
            "WHERE m.movieNm LIKE %:keyword% " +
            "AND m.status = 1")   // 상태가 1인, 즉 보이는 영화만 조회
    Page<Movie> findByMovieNmAndStatus(String keyword, Pageable pageable);
}
