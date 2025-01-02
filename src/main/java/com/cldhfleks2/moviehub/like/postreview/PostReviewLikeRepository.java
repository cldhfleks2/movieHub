package com.cldhfleks2.moviehub.like.postreview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostReviewLikeRepository extends JpaRepository<PostReviewLike, Long> {
    //reviewId로 PostReview List를 가져옴
    @Query("SELECT prl FROM PostReviewLike prl WHERE prl.review.id = :reviewId AND prl.status = 1")
    List<PostReviewLike> findAllByReviewIdAndStatus(Long reviewId);

    //reviewId와 memberId에 해당하는 좋아요 기록을 가져옴. 좋아요 요청되있는것만
    @Query("SELECT prl FROM PostReviewLike prl " +
            "WHERE prl.review.id = :reviewId AND prl.sender.id = :memberId AND prl.status = 1")
    Optional<PostReviewLike> findByReviewIdAndMemberIdAndStatus(Long reviewId, Long memberId);

    //reviewId와 memberId에 해당하는 좋아요 기록을 가져옴.
    @Query("SELECT prl FROM PostReviewLike prl " +
            "WHERE prl.review.id = :reviewId AND prl.sender.id = :memberId")
    Optional<PostReviewLike> findByReviewIdAndMemberId(Long reviewId, Long memberId);
}
