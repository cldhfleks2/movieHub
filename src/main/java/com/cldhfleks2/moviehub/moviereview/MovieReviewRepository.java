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

    //memberId와 일치하는 모든 영화리뷰를 가져옴
    @Query("SELECT mr FROM MovieReview mr WHERE mr.member.id = :memberId AND mr.status = 1")
    Page<MovieReview> findAllByMemberIdAndStatus(Long memberId, Pageable pageable);

    //keyword로 영화리뷰의내용, 영화이름을 검색하는 함수
    @Query("SELECT mr FROM MovieReview mr " +
            "JOIN mr.movie m " +
            "WHERE (mr.content LIKE %:keyword% " +
            "OR m.movieNm LIKE %:keyword% " +
            "OR m.movieNmEn LIKE %:keyword%) " +
            "AND mr.status = 1 " + // 상태가 1인, 즉 보이는 리뷰만 조회
            "AND m.status = 1")   // 상태가 1인, 즉 보이는 영화만 조회
    Page<MovieReview> searchByContentAndMovieNmAndMovieNmEn(String keyword, Pageable pageable);

    //pageable로 (0,count) 갯수를 받아서 그만큼 MovieReviewLike가 많은 순서대로 가져옴
    @Query("SELECT mr FROM MovieReview mr " +
            "LEFT JOIN MovieReviewLike mrl ON mrl.movieReview = mr AND mrl.status = 1 " +
            "WHERE mr.status = 1 " +
            "GROUP BY mr " +
            "ORDER BY COUNT(mrl) DESC")
    Page<MovieReview> findTopMovieReviewsByLikeCount(Pageable pageable);
}
