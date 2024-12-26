package com.cldhfleks2.moviehub.role;

import lombok.Getter;

//권한 부여를 쉽게하기위한 ENUM클래스 - application.properties와 연동
//getRoleName() 으로 값을 가져올 수 있음
@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    VIP("ROLE_VIP"),
    USER("ROLE_USER");

    private final String roleName;
    //생성자
    Role(String roleName) {
        this.roleName = roleName;
    }
}