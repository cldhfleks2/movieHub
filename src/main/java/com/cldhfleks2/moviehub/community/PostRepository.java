package com.cldhfleks2.moviehub.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Query("UPDATE Post p SET p.view = p.view + 1 WHERE p.id = :postId")
    void incrementView(Long postId);

    @Query("SELECT p FROM Post p WHERE p.status = 1")
    Page<Post> findAllAndStatus(Pageable pageable);
}
