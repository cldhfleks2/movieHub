package com.cldhfleks2.moviehub.report.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieReportRepository extends JpaRepository<MovieReport, Long> {
    @Query("SELECT mr FROM MovieReport mr " +
            "WHERE mr.reportDetail LIKE %:keyword% " +
            "AND mr.status = 1")
    Page<MovieReport> findReportDetailByKeyword(String keyword, Pageable pageable);

    @Query("SELECT mr FROM MovieReport mr " +
            "JOIN mr.movie m " +
            "WHERE m.movieNm LIKE %:keyword% " +
            "AND mr.status = 1")
    Page<MovieReport> findMovieNmByKeyword(String keyword, Pageable pageable);
}
