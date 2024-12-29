package com.cldhfleks2.moviehub.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // username로 status=1인 Member 찾기
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.status = 1")
    Optional<Member> findByUsernameAndStatus(String username);

    // username로 Member 찾기
    @Query("SELECT m FROM Member m WHERE m.username = :username")
    Optional<Member> findByUsername(String username);

    // memberId로 status=1인 Member 찾기
    @Query("SELECT m FROM Member m WHERE m.id = :memberId AND m.status = 1")
    Optional<Member> findByIdAndStatus(Long memberId);
}
