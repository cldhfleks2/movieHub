package com.cldhfleks2.moviehub.config;

import com.cldhfleks2.moviehub.movie.quary.KobisQueryCnt;
import com.cldhfleks2.moviehub.movie.quary.KobisQueryCntRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DailyTask {
    private final KobisQueryCntRepository kobisQueryCntRepository;

    //앱 실행되고 무조건 한번 실행
    //현재날짜의 kobisQueryCnt 데이터가 없으면 한번 생성
    @PostConstruct
    public void createKobisQueryCntTask() {
        System.out.println("앱 실행후 kobisQueryCnt 확인");
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"

        Optional<KobisQueryCnt> obj = kobisQueryCntRepository.findByDay(currentDay);
        if(obj.isPresent()) {
            System.out.println("현재 날짜의 kobisQueryCnt가 존재함.");
        }else{
            System.out.println("현재 날짜의 kobisQueryCnt가 없으므로 자동 생성.");
            KobisQueryCnt kobisQueryCnt = new KobisQueryCnt();
            kobisQueryCnt.setDay(currentDay);
            kobisQueryCntRepository.save(kobisQueryCnt);
        }
    }

    //12시간마다 반복되는 작업
    //현재날짜의 kobisQueryCnt이외에 다른 데이터는 전부 삭제
    //매일 쌓여서 DB가 무거워지는것을 막고,
    //DB에서 탐색할때 자원을 아끼기 위해.
    @Scheduled(fixedRate = 43200000)
    public void deletePrevDayKobisQueryCntTask() {
        System.out.println("12시간마다 kobisQueryCnt 확인중..");
        System.out.println("kobisQueryCnt DB에서 현재 날짜와 다른 데이터를 삭제 중...");
        //현재 날짜를 다시 확인
        LocalDate currentDate = LocalDate.now().minusDays(1); //하루 이전 날짜 가져옴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDay = currentDate.format(formatter); //현재 날짜  예) "20241220"
        //현재 날짜와 다른 모든 데이터를 삭제
        kobisQueryCntRepository.deleteAllByDayNotEqual(currentDay);
        System.out.println("현재 날짜와 다른 데이터가 삭제되었습니다.");
    }
}
