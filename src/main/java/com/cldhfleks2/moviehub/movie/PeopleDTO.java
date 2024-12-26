package com.cldhfleks2.moviehub.movie;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PeopleDTO {
    private String peopleNm;
    private Long peopleId;
    private String profilePath;
    private String moviePartNm;
}
