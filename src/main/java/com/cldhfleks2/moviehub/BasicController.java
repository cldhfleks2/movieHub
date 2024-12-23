package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.config.SeleniumWebDriverConfig;
import com.cldhfleks2.moviehub.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
@RequiredArgsConstructor
public class BasicController {
    private final BasicService basicService;

    //test page GET
    @GetMapping("/test")
    String test(Model model) throws Exception {
        return basicService.test(model);
    }

    //test page GET
//    @GetMapping("/test2")
//    String test2(Model model) throws Exception {
//        return basicService.test2(model);
//    }

    //메인 페이지 GET
    @GetMapping({"/", "/main"})
    String getMain(Model model) throws Exception {
        return basicService.getMain(model);
    }

    //영화 상세 페이지 GET
    @GetMapping("/detail/{movieCd}")
    String getDetail(@PathVariable String movieCd, Model model, RedirectAttributes redirectAttributes)  throws Exception{
        return basicService.getDetail(movieCd, model, redirectAttributes);
    }
}
