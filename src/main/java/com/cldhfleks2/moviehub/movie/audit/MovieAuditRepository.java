package com.cldhfleks2.moviehub.movie.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieAuditRepository extends JpaRepository<MovieAudit, Long> {

}
