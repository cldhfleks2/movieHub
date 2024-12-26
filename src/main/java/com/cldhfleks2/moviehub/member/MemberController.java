package com.cldhfleks2.moviehub.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //회원가입시 아이디 중복검사
    @PostMapping("/api/checkUsername")
    ResponseEntity<String> checkUsername(String username) {
        return  memberService.checkUsername(username);
    }

    //로그인 페이지 GET
    @GetMapping("/login")
    String getLogin(Authentication auth){
        return memberService.getLogin(auth);
    }

    //회원가입 페이지 GET
    @GetMapping("/register")
    String getRegister (Authentication auth) {
        return memberService.getRegister(auth);
    }

    //회원가입
    @PostMapping("/register")
    ResponseEntity<String> register(Member member) {
        return memberService.register(member);
    }


}

