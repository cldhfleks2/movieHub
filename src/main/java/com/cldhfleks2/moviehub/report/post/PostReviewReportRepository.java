package com.cldhfleks2.moviehub.report.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostReviewReportRepository extends JpaRepository<PostReviewReport, Long> {
    //신고내역의 reportDetail로 검색
    @Query("SELECT prr FROM PostReviewReport prr WHERE prr.reportDetail LIKE %:keyword% AND prr.status = 1")
    Page<PostReviewReport> findByReportDetailContaining(String keyword, Pageable pageable);

    //게시글의 title으로 검색
    @Query("SELECT prr FROM PostReviewReport prr " +
            "JOIN prr.review r " +
            "JOIN r.post p " +
            "WHERE p.title LIKE %:keyword% " +
            "AND prr.status = 1 " +
            "AND r.status = 1 " +
            "AND p.status = 1 ")
    Page<PostReviewReport> findByReviewPostTitleContaining(String keyword, Pageable pageable);

    //댓글의 content으로 검색
    @Query("SELECT prr FROM PostReviewReport prr " +
            "JOIN prr.review r " +
            "WHERE r.content LIKE %:keyword% " +
            "AND prr.status = 1 " +
            "AND r.status = 1 ")
    Page<PostReviewReport> findByReviewContentContaining(String keyword, Pageable pageable);

    //댓글의 nickname으로 검색
    @Query("SELECT prr FROM PostReviewReport prr " +
            "JOIN prr.review r " +
            "JOIN r.member m " +
            "WHERE m.nickname LIKE %:keyword% " +
            "AND prr.status = 1 " +
            "AND r.status = 1 " +
            "AND m.status = 1 ")
    Page<PostReviewReport> findByReviewMemberNicknameContaining(String keyword, Pageable pageable);
}
