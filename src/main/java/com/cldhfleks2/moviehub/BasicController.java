package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.config.SeleniumWebDriverConfig;
import com.cldhfleks2.moviehub.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
@RequiredArgsConstructor
public class BasicController {
    private final BasicService basicService;

    @GetMapping("/test")
    String test(Model model) throws Exception {
        return basicService.test(model);
    }

    @GetMapping({"/", "/main"})
    String getMain(Model model) throws Exception {
        return basicService.getMain(model);
    }

    @GetMapping("/detail/{movieCd}")
    String getDetail(@PathVariable String movieCd, Model model) {
        return basicService.getDetail(movieCd, model);
    }

    //JS에서 KOBIS Key를 요청하면 String으로 전달해준다.
    @GetMapping("/getkobiskey")
    ResponseEntity<String> getKobiskey() {
        return basicService.getKobiskey();
    }
}
