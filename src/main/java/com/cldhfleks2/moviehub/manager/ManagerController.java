package com.cldhfleks2.moviehub.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;

    //관리자 페이지 GET
    @GetMapping("/manager")
    String getManager(Authentication auth, Model model) {
        return managerService.getManager(auth, model);
    }

}
