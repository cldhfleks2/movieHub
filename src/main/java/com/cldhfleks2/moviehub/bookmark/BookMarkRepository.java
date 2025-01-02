package com.cldhfleks2.moviehub.bookmark;

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
    @Query("SELECT bm FROM BookMark bm " +
            "JOIN bm.member m " +
            "JOIN bm.movie mv " +
            "WHERE m.username = :username " +
            "AND bm.status = 1 " +
            "AND mv.status = 1")
    Page<BookMark> findAllByUsernameAndStatus(String username, Pageable pageable);
}
