package com.cldhfleks2.moviehub.role;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RoleService {

    @Value("${role.admin}")
    private String[] adminUsers;

    @Value("${role.vip}")
    private String[] vipUsers;

    // 관리자인지 확인하는 메서드
    public Role getUserRole(String username) {
        if (Arrays.asList(adminUsers).contains(username)) {
            return Role.ADMIN;
        } else if (Arrays.asList(vipUsers).contains(username)) {
            return Role.VIP;
        } else {
            return Role.USER;
        }
    }
}