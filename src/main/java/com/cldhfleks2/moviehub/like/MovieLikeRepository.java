package com.cldhfleks2.moviehub.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
    // username과 movieCd으로  MovieLike 찾기
    @Query("SELECT ml FROM MovieLike ml WHERE ml.member.username = :username AND ml.movie.movieCd = :movieCd")
    Optional<MovieLike> findByUsernameAndMovieCd(String username, String movieCd);

    // movieCd로 모든 MovieLike 찾기
    @Query("SELECT ml FROM MovieLike ml WHERE ml.movie.movieCd = :movieCd AND ml.status = 1")
    List<MovieLike> findAllByMovieCdAndStatus(String movieCd);
}
