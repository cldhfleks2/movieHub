package com.cldhfleks2.moviehub.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
    // movieId로 객체를 조회하는 쿼리
    @Query("SELECT ml FROM MovieLike ml WHERE ml.movie.id = :movieId AND ml.status = 1")
    List<MovieLike> findByMovieIdAndStatus(Long movieId);

    // member.username으로 MovieLike 찾기
    @Query("SELECT ml FROM MovieLike ml WHERE ml.member.username = :username")
    Optional<MovieLike> findByUsername(String username);

    // movieCd로 모든 MovieLike 찾기
    @Query("SELECT ml FROM MovieLike ml WHERE ml.movie.movieCd = :movieCd AND ml.status = 1")
    List<MovieLike> findAllByMovieCdAndStatus(String movieCd);

}
