package com.cldhfleks2.moviehub.member;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class MemberDetail extends User {
    private String username;
    private String nickname;
    private String role;
    private String profileImage;  // 프로필 이미지

    public MemberDetail(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}
