package com.cldhfleks2.moviehub.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    //postId로 PostReview List를 가져옴
    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId AND pl.status = 1")
    List<PostLike> findAllByPostIdAndStatus(Long postId);

    //postId와 memberId에 해당하는 좋아요 기록을 가져옴. 좋아요 요청되있는것만
    @Query("SELECT pl FROM PostLike pl " +
            "WHERE pl.post.id = :postId AND pl.sender.id = :memberId AND pl.status = 1")
    Optional<PostLike> findByPostIdAndMemberIdAndStatus(Long postId, Long memberId);

    //postId와 memberId에 해당하는 좋아요 기록을 가져옴.
    @Query("SELECT pl FROM PostLike pl " +
            "WHERE pl.post.id = :postId AND pl.sender.id = :memberId")
    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);
}
