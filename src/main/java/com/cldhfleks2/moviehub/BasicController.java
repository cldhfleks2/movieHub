package com.cldhfleks2.moviehub;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
@RequiredArgsConstructor
public class BasicController {
    private final BasicService basicService;

    @GetMapping({"/", "/main"})
    String getMain(Model model) throws Exception {
        return basicService.getMain(model);
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
