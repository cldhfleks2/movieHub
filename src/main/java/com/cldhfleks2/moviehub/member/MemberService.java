package com.cldhfleks2.moviehub.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    //회원가입시 아이디 중복검사
    ResponseEntity<String> checkUsername(String username) {
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);

        if(memberObj.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); //중복됨을 전달
        }else{
            return ResponseEntity.status(HttpStatus.OK).build(); //사용가능을 전달
        }
    }

    //로그인 페이지 GET
    String getLogin(Authentication auth){
        //인증정보가 없거나 로그인된 상태이면 메인 화면으로 이동
        if(auth != null && auth.isAuthenticated()) return "redirect:/main";
        return "member/login";
    }
}
