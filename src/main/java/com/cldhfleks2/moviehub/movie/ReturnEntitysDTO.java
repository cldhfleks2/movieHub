package com.cldhfleks2.moviehub.movie;

import com.cldhfleks2.moviehub.movie.actor.MovieActor;
import com.cldhfleks2.moviehub.movie.audit.MovieAudit;
import com.cldhfleks2.moviehub.movie.company.MovieCompany;
import com.cldhfleks2.moviehub.movie.dailystat.MovieDailyStat;
import com.cldhfleks2.moviehub.movie.director.MovieDirector;
import com.cldhfleks2.moviehub.movie.genre.MovieGenre;
import com.cldhfleks2.moviehub.movie.nation.MovieNation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReturnEntitysDTO {
    Movie movie;
    MovieDailyStat movieDailyStat;
    List<MovieDirector> movieDirectorList;
    List<MovieActor> movieActorList;
    List<MovieNation> movieNationList;
    List<MovieGenre> movieGenreList;
    List<MovieCompany> movieCompanyList;
    List<MovieAudit> movieAuditList;
}
