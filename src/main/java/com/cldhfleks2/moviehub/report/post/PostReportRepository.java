package com.cldhfleks2.moviehub.report.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    //신고내역의 reportDetail로 검색
    @Query("SELECT pr FROM PostReport pr WHERE pr.reportDetail LIKE %:keyword% AND pr.status = 1")
    Page<PostReport> findByReportDetailContaining(String keyword, Pageable pageable);

    //게시글의 title으로 검색
    @Query("SELECT pr FROM PostReport pr " +
            "JOIN pr.post p " +
            "WHERE p.title LIKE %:keyword% " +
            "AND pr.status = 1" +
            "AND p.status = 1")
    Page<PostReport> findByPostTitleContaining(String keyword, Pageable pageable);

    //게시글의 content로 검색
    @Query("SELECT pr FROM PostReport pr " +
            "JOIN pr.post p " +
            "WHERE p.content LIKE %:keyword% " +
            "AND pr.status = 1" +
            "AND p.status = 1")
    Page<PostReport> findByPostContentContaining(String keyword, Pageable pageable);

    //작성자의 nickname로 검색
    @Query("SELECT pr FROM PostReport pr " +
            "JOIN pr.post.member m " +
            "WHERE m.nickname LIKE %:keyword% " +
            "AND pr.status = 1" +
            "AND m.status = 1")
    Page<PostReport> findByMemberNicknameContaining(String keyword, Pageable pageable);
}
