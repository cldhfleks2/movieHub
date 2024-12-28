package com.cldhfleks2.moviehub.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieReviewRepository extends JpaRepository<MovieReview, Long> {
    //movieCd로 MovieReview리스트를 가져오는 쿼리
    @Query("SELECT mr FROM MovieReview mr WHERE mr.movie.movieCd = :movieCd AND mr.status = 1")
    Page<MovieReview> findAllByMovieCdAndStatus(String movieCd, Pageable pageable);

    //searchText와 부분일치하는 MovieReview리스트를 가져오는 쿼리
    //movie.movieNm,  member.nickname중 검색
    @Query("""
        SELECT mr 
        FROM MovieReview mr
        JOIN mr.movie movie
        JOIN mr.member member
        WHERE (:searchText IS NULL OR :searchText = ''\s
                   OR LOWER(movie.movieNm) LIKE LOWER(CONCAT('%', :searchText, '%'))\s
                   OR LOWER(member.nickname) LIKE LOWER(CONCAT('%', :searchText, '%')))
                           """)
    Page<MovieReview> search(String searchText, Pageable pageable);
}
