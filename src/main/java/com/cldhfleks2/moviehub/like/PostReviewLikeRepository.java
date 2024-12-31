package com.cldhfleks2.moviehub.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostReviewLikeRepository extends JpaRepository<PostReviewLike, Long> {
    //postId로 PostReview List를 가져오되, 생성 날짜로 정렬
    @Query("SELECT prl FROM PostReviewLike prl WHERE prl.review.id = :reviewId AND prl.status = 1")
    List<PostReviewLike> findAllByReviewId(Long reviewId);
}
