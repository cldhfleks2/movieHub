package com.cldhfleks2.moviehub.bookmark;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService;

}
