package com.cldhfleks2.moviehub.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostReviewRepository extends JpaRepository<PostReview, Long> {
    //postId로 PostReview List를 가져오되, 생성 날짜로 정렬
    @Query("SELECT pr FROM PostReview pr WHERE pr.post.id = :postId AND pr.status = 1 ORDER BY pr.createDate DESC")
    List<PostReview> findAllByPostId(Long postId);
}
