package com.cldhfleks2.moviehub.movie.dailystat;

import com.cldhfleks2.moviehub.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieDailyStatRepository extends JpaRepository<MovieDailyStat, Long> {
    // day와 Movie_id로 객체를 조회하는 쿼리
    @Query("SELECT mds FROM MovieDailyStat mds WHERE mds.day = :day AND mds.movie.id = :movieId AND mds.status = 1")
    Optional<MovieDailyStat> findByDayAndMovieIdAndStatus(String day, Long movieId);

    // movie_id로 객체를 조회하는 쿼리
    @Query("SELECT m FROM MovieDailyStat m WHERE m.movie.id = :movieId AND m.status = 1")
    List<MovieDailyStat> findByMovieIdAndStatus(Long movieId);

}
