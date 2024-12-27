package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.member.MemberDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {

    // 모든 요청에서 principal을 자동으로 모델에 추가
    @ModelAttribute("principal")
    public MemberDetail addPrincipal(Authentication auth) {
        if(auth == null) return null; //인증 정보가 있을 때만
        // Authentication에서 CustomUserDetails (MemberDetail) 객체를 가져옴
        MemberDetail principal = (MemberDetail) auth.getPrincipal();
        return principal; // 모델에 principal을 추가
    }
}
