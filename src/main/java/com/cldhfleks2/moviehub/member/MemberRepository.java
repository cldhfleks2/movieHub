package com.cldhfleks2.moviehub.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // username과 일치하는 Member 찾기
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.status = 1")
    Optional<Member> findByUsernameAndStatus(String username);
}
