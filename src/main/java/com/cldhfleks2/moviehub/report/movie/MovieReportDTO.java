package com.cldhfleks2.moviehub.report.movie;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovieReportDTO {
    private String movieCd;

    private Boolean POSTER;   // 주요 스포일러 포함
    private Boolean MOVIENAME;     // 잘못된 정보
    private Boolean MOVIEPEOPLE; // 무관한 내용/광고성
    private Boolean HARMFUL;   // 유해하거나 불건전한 내용
    private Boolean HATE;      // 혐오 발언

    private String reportDetail;
}
