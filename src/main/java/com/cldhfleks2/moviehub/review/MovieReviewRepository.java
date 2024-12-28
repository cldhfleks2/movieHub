package com.cldhfleks2.moviehub.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieReviewRepository extends JpaRepository<MovieReview, Long> {
    //movieCd로 MovieReview리스트를 가져오는 쿼리
    @Query("SELECT mr FROM MovieReview mr WHERE mr.movie.movieCd = :movieCd AND mr.status = 1")
    Page<MovieReview> findAllByMovieCdAndStatus(String movieCd, Pageable pageable);
}
