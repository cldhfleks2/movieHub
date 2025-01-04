package com.cldhfleks2.moviehub.moviereview;

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
    SELECT mr FROM MovieReview mr 
    JOIN mr.movie movie 
    JOIN mr.member member
    WHERE mr.status = 1 
    AND movie.status = 1 
    AND member.status = 1
    AND (:searchText IS NULL OR :searchText = '' 
    OR LOWER(movie.movieNm) LIKE LOWER(CONCAT('%', :searchText, '%')) 
    OR LOWER(member.nickname) LIKE LOWER(CONCAT('%', :searchText, '%')))
    """)
    Page<MovieReview> search(String searchText, Pageable pageable);

    @Query("SELECT mr FROM MovieReview mr WHERE mr.member.id = :memberId AND mr.status = 1")
    Page<MovieReview> findAllByMemberIdAndStatus(Long memberId, Pageable pageable);

    @Query("SELECT mr FROM MovieReview mr " +
            "JOIN mr.movie m " +
            "WHERE (mr.content LIKE %:keyword% " +
            "OR m.movieNm LIKE %:keyword% " +
            "OR m.movieNmEn LIKE %:keyword%) " +
            "AND mr.status = 1 " + // 상태가 1인, 즉 보이는 리뷰만 조회
            "AND m.status = 1")   // 상태가 1인, 즉 보이는 영화만 조회
    Page<MovieReview> searchByContentAndMovieNmAndMovieNmEn(String keyword, Pageable pageable);
}
