package com.cldhfleks2.moviehub.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // movieCd로 Movie 객체를 조회하는 쿼리
    //status=0 인 것들도 가져온다.
    @Query("SELECT m FROM Movie m WHERE m.movieCd = :movieCd")
    Optional<Movie> findByMovieCd(String movieCd);

    // movieCd로 Movie 객체를 조회하는 쿼리
    //status=1 인 것만 가져온다.
    @Query("SELECT m FROM Movie m WHERE m.movieCd = :movieCd AND m.status = 1")
    Optional<Movie> findByMovieCdAndStatus(String movieCd);
}
