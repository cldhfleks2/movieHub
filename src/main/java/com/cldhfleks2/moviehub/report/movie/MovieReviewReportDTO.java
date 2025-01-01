package com.cldhfleks2.moviehub.report.movie;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovieReviewReportDTO {
    private Long movieReviewId;

    private Boolean SPOILER;
    private Boolean WRONG;
    private Boolean UNRELATED;
    private Boolean HARMFUL;
    private Boolean HATE;
    private Boolean COPYRIGHT;
    private Boolean SPAM;

    private String reportDetail;
}