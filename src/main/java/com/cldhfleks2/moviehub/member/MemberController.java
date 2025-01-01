package com.cldhfleks2.moviehub.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

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
    String getMyPage(Model model, Authentication auth, String keyword, Integer pageIdx, String sort, String category) {
        return memberService.getMyPage(model, auth, keyword, pageIdx, sort, category);
    }

    //유저 프로필 GET
    @GetMapping("/userprofile/{memberId}")
    String getUserprofile(@PathVariable Long memberId, Model model) {
        return memberService.getUserprofile(memberId, model);
    }

    //내가 찜한 리스트 GET
    @GetMapping("/mywish")
    String getMyWish(Model model, Authentication auth, Integer pageIdx) throws Exception{
        return memberService.getMyWish(model, auth, pageIdx);
    }

    //유저 프로필 수정 요청
    @PutMapping("/api/user/profile/edit")
    ResponseEntity<String> editUserprofile(String nickname, String password, MultipartFile profileImage, Authentication auth)  throws Exception{
        return memberService.editUserprofile(nickname, password, profileImage, auth);
    }

}

