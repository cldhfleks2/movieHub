package com.cldhfleks2.moviehub.movie.quary;

import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KobisQueryCntRepository extends JpaRepository<KobisQueryCnt, Long> {
    // day로 객체를 조회하는 쿼리
    @Query("SELECT k FROM KobisQueryCnt k WHERE k.day = :day AND k.status = 1")
    Optional<KobisQueryCnt> findByDay(String day);

    // day와 다른 모든 기록을 지움. => DB과부하를 방지하기 위함
    @Query("DELETE FROM KobisQueryCnt k WHERE k.day <> :day AND k.status = 1")
    @Modifying
    @Transactional
    void deleteAllByDayNotEqual(String day);
}
