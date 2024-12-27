package com.cldhfleks2.moviehub.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    // member.username으로 MovieLike 찾기
    @Query("SELECT bm FROM BookMark bm WHERE bm.member.username = :username")
    Optional<BookMark> findByUsername(String username);
}
