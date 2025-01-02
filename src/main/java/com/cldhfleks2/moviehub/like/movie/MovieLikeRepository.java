package com.cldhfleks2.moviehub.like.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // username으로 모든 MovieLike 찾기 이때 MovieLike, Movie모두 status=1인것만 가져온다.
    @Query("SELECT ml FROM MovieLike ml " +
            "JOIN ml.member m " +
            "JOIN ml.movie mv " +
            "WHERE m.username = :username " +
            "AND ml.status = 1 " +
            "AND mv.status = 1")
    Page<MovieLike> findAllByUsernameAndStatus(String username, Pageable pageable);

}
