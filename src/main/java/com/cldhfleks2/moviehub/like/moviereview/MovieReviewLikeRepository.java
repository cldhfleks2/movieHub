package com.cldhfleks2.moviehub.like.moviereview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieReviewLikeRepository extends JpaRepository<MovieReviewLike, Long> {
    //movieReviewId로 MovieReviewLike 리스트를 조회
    @Query("SELECT mrl FROM MovieReviewLike mrl WHERE mrl.movieReview.id = :movieReviewId AND mrl.status = 1")
    List<MovieReviewLike> findAllByMovieReviewIdAndStatus(Long movieReviewId);

    //username과 movieReviewId로 MovieReviewLike 리스트를 조회
    @Query("SELECT mrl FROM MovieReviewLike mrl WHERE mrl.member.username = :username AND mrl.movieReview.id = :movieReviewId")
    Optional<MovieReviewLike> findByUsernameAndMovieReviewId(String username, Long movieReviewId);

}
