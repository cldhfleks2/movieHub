package com.cldhfleks2.moviehub.movie;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE movie SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
//몇개 필드 빼고는 잘 안바뀌는 엔티티
public class Movie {
    //일별박스오피스, 영화상세정보
    private String movieCd; // 영화 고유 코드  예) "20200142"
    private String movieNm; // 영화 이름
    //예) "20241204" 영화상세정보처럼 저장할거다.
    private String openDt; // 개봉일     예) "2024-12-04(일별박스오피스)" "20241204(영화상세정보)"

    //일별박스오피스
    //이것들은 박스오피스 목록에 들은 영화만 존재하는 값
    //영화 상세정보 요청등에서는 얻을 수 없는 값임.
    private String salesAcc; // 누적 매출액     예) "20542011020"
    private String audiAcc; // 누적 관객 수       예) "2191719"

    //영화상세정보
    private String movieNmEn; // 영화 이름 영문  예) "소방관"
    private String prdtYear; // 제작 연도       예) "2024"
    private String showTm; // 영화 상영 시간     예) "106" 단위 분
    private String typeNm; // 영화 유형         예) "장편"

    //1차 : 셀레니움 웹 크롤링
    //2차 : TMDB에서 영화 한글 명으로 검색
    //3차 : TMDB에서 영화 영문 명으로 검색
    private String posterURL;

    //아래는 기본 필드들
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;
    @UpdateTimestamp
    private LocalDateTime updateDate;
    private int status = 1; //존재 여부 (1:보임 0:삭제)
}
