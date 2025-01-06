package com.cldhfleks2.moviehub.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
    //조회수+1
    @Modifying
    @Query("UPDATE Post p SET p.view = p.view + 1 WHERE p.id = :postId")
    void incrementView(Long postId);

    //keyword, status=1 page검색
    @Query("SELECT p FROM Post p WHERE p.status = 1 AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findAllByKeywordAndStatus(String keyword, Pageable pageable);

    //keyword, category, status=1 page검색
    @Query("SELECT p FROM Post p WHERE p.status = 1 AND p.postType = :category AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findAllByKeywordAndCategoryAndStatus(String keyword, PostType category, Pageable pageable);

    //memberId로 page검색
    @Query("SELECT p FROM Post p WHERE p.member.id = :memberId AND p.status = 1")
    Page<Post> findAllByMemberIdAndStatus(Long memberId, Pageable pageable);

    //pageable로 (0,count) 갯수를 받아서 그만큼 Post를 좋아요 많은 순서대로 가져옴
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN PostLike pl ON pl.post = p AND pl.status = 1 " +
            "WHERE p.status = 1 " +
            "GROUP BY p " +
            "ORDER BY COUNT(pl) DESC")
    Page<Post> findTopPostsByLikeCount(Pageable pageable);
}
