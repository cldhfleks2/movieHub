package com.cldhfleks2.moviehub.bookmark;

import com.cldhfleks2.moviehub.like.movie.MovieLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    // username과 movieCd으로  MovieLike 찾기
    @Query("SELECT bm FROM BookMark bm WHERE bm.member.username = :username AND bm.movie.movieCd = :movieCd")
    Optional<BookMark> findByUsernameAndMovieCd(String username, String movieCd);

    // username으로 모든 MovieLike 찾기 이때 MovieLike, Movie모두 status=1인것만 가져온다.
    @Query("SELECT ml FROM MovieLike ml " +
            "JOIN ml.member m " +
            "JOIN ml.movie mv " +
            "WHERE m.username = :username " +
            "AND ml.status = 1 " +
            "AND mv.status = 1")
    Page<MovieLike> findAllByUsernameAndStatus(String username, Pageable pageable);
}
