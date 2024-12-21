package com.cldhfleks2.moviehub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicService {

    String getMain() {
        return "main/main";
    }

    String getDetail() {
        return "detail/detail";
    }
}
