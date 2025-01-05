package com.cldhfleks2.moviehub.report.movie;

import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.movie.Movie;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MovieReportDTO {
    private String movieCd;

    private Boolean POSTER;   // 영화 포스터가 다릅니다.
    private Boolean MOVIENAME;     // 영화 제목이 다릅니다.
    private Boolean MOVIEPEOPLE; // 인물 정보가 잘못 되었습니다.
    private Boolean HARMFUL;   // 유해하거나 불건전한 내용이 포함되있습니다.
    private Boolean HATE;      // 불건전한 내용입니다.

    private String reportDetail;

    private Movie movie;
    private Member member;
    private LocalDateTime updateDate;
    private boolean status;
}
