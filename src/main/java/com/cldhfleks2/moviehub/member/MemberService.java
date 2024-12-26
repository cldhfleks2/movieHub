package com.cldhfleks2.moviehub.member;

import com.cldhfleks2.moviehub.error.ErrorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
        //인증정보가 있거나 로그인된 상태이면 메인 화면으로 이동
        if(auth != null && auth.isAuthenticated()) return "redirect:/main";
        return "member/login";
    }

    //회원가입 페이지 GET
    String getRegister (Authentication auth) {
        //인증정보가 있거나 로그인된 상태이면 메인 화면으로 이동
        if(auth != null && auth.isAuthenticated()) return "redirect:/main";
        return "member/register";
    }

    //회원가입 : DB에 저장
    @Transactional
    ResponseEntity<String> register (Member member) {
        //아이디 중복확인은 이미 거친 상태

        //비밀번호 암호화
        String passwordEncoded = passwordEncoder.encode(member.getPassword());
        member.setPassword(passwordEncoded);
        //DB저장
        memberRepository.save(member);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }



}
