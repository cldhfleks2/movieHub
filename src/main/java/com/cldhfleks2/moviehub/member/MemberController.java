package com.cldhfleks2.moviehub.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

    //회원가입 : DB에 저장
    @PostMapping("/register")
    ResponseEntity<String> register(Member member) {
        return memberService.register(member);
    }

    //마이페이지 GET
    @GetMapping("/mypage")
    String getMyPage(Model model, Authentication auth) {
        return memberService.getMyPage(model, auth);
    }

    //유저 프로필 GET
    @GetMapping("/userprofile/{memberId}")
    String getUserprofile(@PathVariable Long memberId, Model model, Authentication auth) {
        return memberService.getUserprofile(memberId, model, auth);
    }

}

