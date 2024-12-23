package com.cldhfleks2.moviehub.movie.actor;

import com.cldhfleks2.moviehub.movie.director.MovieDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieActorRepository extends JpaRepository<MovieActor, Long> {
    // movieId로 MovieActor 객체를 조회하는 쿼리
    //status=1 인 것만 가져온다.
    @Query("SELECT m FROM MovieActor m WHERE m.movie.id = :movieId AND m.status = 1")
    List<MovieActor> findByMovieIdAndStatus(Long movieId);
}
