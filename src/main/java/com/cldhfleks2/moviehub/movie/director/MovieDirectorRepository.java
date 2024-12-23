package com.cldhfleks2.moviehub.movie.director;

import com.cldhfleks2.moviehub.movie.audit.MovieAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieDirectorRepository extends JpaRepository<MovieDirector, Long> {
    // movieId로 MovieDirector 객체를 조회하는 쿼리
    //status=1 인 것만 가져온다.
    @Query("SELECT m FROM MovieDirector m WHERE m.movie.id = :movieId AND m.status = 1")
    List<MovieDirector> findByMovieIdAndStatus(Long movieId);
}
