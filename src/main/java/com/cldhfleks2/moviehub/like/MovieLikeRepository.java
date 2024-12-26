package com.cldhfleks2.moviehub.like;

import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
    // movie_id로 객체를 조회하는 쿼리
    @Query("SELECT m FROM MovieLike m WHERE m.movie.id = :movieId AND m.status = 1")
    List<MovieLike> findByMovieIdAndStatus(Long movieId);
}
