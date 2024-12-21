package com.cldhfleks2.moviehub;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    //JS에서 KOBIS Key를 요청하면 String으로 전달해준다.
    @GetMapping("/getkobiskey")
    ResponseEntity<String> getKobiskey() {
        return basicService.getKobiskey();
    }
}
