package com.cldhfleks2.moviehub.postreview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostReviewRepository extends JpaRepository<PostReview, Long> {
    //postId로 PostReview List를 가져오되, 생성 날짜로 정렬
    @Query("SELECT pr FROM PostReview pr WHERE pr.post.id = :postId AND pr.status = 1 ORDER BY pr.createDate DESC")
    List<PostReview> findAllByPostIdAndStatus(Long postId);

    //parent가 부모댓글인 모든 댓글List를 검색
    @Query("SELECT pr FROM PostReview pr WHERE pr.parent = :parent AND pr.status = 1")
    List<PostReview> findAllByParentAndStatus(PostReview parent);

    //keyword, memberId로 댓글내용, 부모댓글 작성자 nickname 검색
    @Query("SELECT pr FROM PostReview pr " +
            "WHERE pr.status = 1 " +
            "AND (pr.content LIKE %:keyword%) " +
            "AND (pr.member.id = :memberId)")
    Page<PostReview> findAllByKeywordAndStatus(String keyword, Long memberId,  Pageable pageable);

    //memberId로 PostReview검색
    @Query("SELECT pr FROM PostReview pr WHERE pr.member.id = :memberId AND pr.status = 1")
    Page<PostReview> findAllByMemberIdAndStatus(Long memberId, Pageable pageable);
}
