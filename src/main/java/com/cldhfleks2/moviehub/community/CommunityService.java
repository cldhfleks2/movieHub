package com.cldhfleks2.moviehub.community;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class CommunityService {

    //커뮤니티 페이지 GET
    String getCommunity(Model model, Authentication auth) {





        return "community/community";
    }

}
