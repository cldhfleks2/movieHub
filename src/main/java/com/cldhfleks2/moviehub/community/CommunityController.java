package com.cldhfleks2.moviehub.community;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    //커뮤니티 페이지 GET
    @GetMapping("/community")
    String getCommunity(Model model, Authentication auth) {
        return communityService.getCommunity(model, auth);
    }

    //게시글 상세 페이지 GET
    @GetMapping("/postDetail/")
    String getPostDetail(Model model, Authentication auth) {
        return communityService.getPostDetail(model, auth);
    }

}
