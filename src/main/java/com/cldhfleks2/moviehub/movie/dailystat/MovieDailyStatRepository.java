package com.cldhfleks2.moviehub.movie.dailystat;

import com.cldhfleks2.moviehub.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MovieDailyStatRepository extends JpaRepository<MovieDailyStat, Long> {
    // day로 객체를 조회하는 쿼리
    @Query("SELECT m FROM MovieDailyStat m WHERE m.day = :day AND m.status = 1")
    Optional<MovieDailyStat> findByDay(String day);
}
