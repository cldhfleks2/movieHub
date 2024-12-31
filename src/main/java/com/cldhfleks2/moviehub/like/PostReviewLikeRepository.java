package com.cldhfleks2.moviehub.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostReviewLikeRepository extends JpaRepository<PostReviewLike, Long> {
    //postId로 PostReview List를 가져오되, 생성 날짜로 정렬
    @Query("SELECT prl FROM PostReviewLike prl WHERE prl.review.id = :reviewId AND prl.status = 1")
    List<PostReviewLike> findAllByReviewId(Long reviewId);

    //reviewId와 memberId에 해당하는 좋아요 기록을 가져옴. 좋아요 요청되있는것만
    @Query("SELECT prl FROM PostReviewLike prl " +
            "WHERE prl.review.id = :reviewId AND prl.sender.id = :memberId AND prl.status = 1")
    Optional<PostReviewLike> findByReviewIdAndMemberId(Long reviewId, Long memberId);
}
