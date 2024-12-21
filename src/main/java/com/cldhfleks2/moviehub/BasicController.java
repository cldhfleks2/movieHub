package com.cldhfleks2.moviehub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BasicController {
    private final BasicService basicService;

    @GetMapping({"/", "/main"})
    String getMain() {
        return basicService.getMain();
    }

    @GetMapping("/detail")
    String getDetail() {
        return basicService.getDetail();
    }
}
