package com.cldhfleks2.moviehub.movie.audit;

import com.cldhfleks2.moviehub.movie.genre.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieAuditRepository extends JpaRepository<MovieAudit, Long> {
    // movieId로 MovieAudit 객체를 조회하는 쿼리
    //status=1 인 것만 가져온다.
    @Query("SELECT m FROM MovieAudit m WHERE m.movie.id = :movieId AND m.status = 1")
    List<MovieAudit> findByMovieIdAndStatus(Long movieId);
}
