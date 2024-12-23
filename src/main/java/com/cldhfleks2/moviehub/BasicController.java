package com.cldhfleks2.moviehub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    //검색 페이지 GET
    @GetMapping("/search")
    String getSearch(String keyword, Integer pageIdx, Model model)  throws Exception{
        return basicService.getSearch(keyword, pageIdx, model);
    }

}
