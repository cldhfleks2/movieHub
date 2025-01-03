package com.cldhfleks2.moviehub.movie.genre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    // movieId로 MovieGenre 객체를 조회하는 쿼리
    //status=1 인 것만 가져온다.
    @Query("SELECT m FROM MovieGenre m WHERE m.movie.id = :movieId AND m.status = 1")
    List<MovieGenre> findByMovieIdAndStatus(Long movieId);
}
