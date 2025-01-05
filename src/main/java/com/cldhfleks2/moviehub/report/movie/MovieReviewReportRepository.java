package com.cldhfleks2.moviehub.report.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieReviewReportRepository extends JpaRepository<MovieReviewReport, Long> {
    //신고내역의 reportDetail로 검색
    @Query("SELECT mrr FROM MovieReviewReport mrr WHERE mrr.reportDetail LIKE %:keyword% AND mrr.status = 1")
    Page<MovieReviewReport> findByReportDetailContaining(String keyword, Pageable pageable);

    //영화의 movieNm으로 검색
    @Query("SELECT mrr FROM MovieReviewReport mrr " +
            "JOIN mrr.movieReview mr " +
            "JOIN mr.movie movie " +
            "WHERE movie.movieNm LIKE %:keyword% AND mrr.status = 1")
    Page<MovieReviewReport> findByMovieNmContaining(String keyword, Pageable pageable);

    //리뷰 작성자의 nickname으로 검색
    @Query("SELECT mrr FROM MovieReviewReport mrr " +
            "JOIN mrr.member m " +
            "WHERE m.nickname LIKE %:keyword% " +
            "AND mrr.status = 1 " +
            "AND m.status = 1")
    Page<MovieReviewReport> findByMemberNicknameContaining(String keyword, Pageable pageable);

    //리뷰 내용으로 검색
    @Query("SELECT mrr FROM MovieReviewReport mrr " +
            "JOIN mrr.movieReview mr " +
            "WHERE mr.content LIKE %:keyword% " +
            "AND mrr.status = 1" +
            "AND mr.status = 1")
    Page<MovieReviewReport> findByMovieReviewContentContaining(String keyword, Pageable pageable);
}
