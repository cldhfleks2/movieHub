package com.cldhfleks2.moviehub.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberDTO {
    private String username;
    private String nickname;
    private String profileImage;
}
