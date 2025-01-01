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

    //모든 status=1인 post page를 가져오는
    @Query("SELECT p FROM Post p WHERE p.status = 1 AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findAllByKeywordAndStatus(String keyword, Pageable pageable);

    //category가 일치하는 status=1인 post page를 가져옴
    @Query("SELECT p FROM Post p WHERE p.status = 1 AND p.postType = :category AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findAllByKeywordAndCategoryAndStatus(String keyword, PostType category, Pageable pageable);
}
