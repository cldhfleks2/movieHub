package com.cldhfleks2.moviehub.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class MemberDTO {
    private Long id;
    private String username;
    private String nickname;
    private String profileImage;
}
