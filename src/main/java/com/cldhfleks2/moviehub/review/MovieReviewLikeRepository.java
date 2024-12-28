package com.cldhfleks2.moviehub.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieReviewLikeRepository extends JpaRepository<MovieReviewLike, Long> {
    //movieReviewId로 MovieReviewLike 리스트를 조회
    @Query("SELECT mrl FROM MovieReviewLike mrl WHERE mrl.movieReview.id = :movieReviewId AND mrl.status = 1")
    List<MovieReviewLike> findAllByMovieReviewIdAndStatus(Long movieReviewId);
}
