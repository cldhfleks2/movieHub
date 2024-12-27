package com.cldhfleks2.moviehub.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    // username과 movieCd으로  MovieLike 찾기
    @Query("SELECT bm FROM BookMark bm WHERE bm.member.username = :username AND bm.movie.movieCd = :movieCd")
    Optional<BookMark> findByUsernameAndMovieCd(String username, String movieCd);
}
