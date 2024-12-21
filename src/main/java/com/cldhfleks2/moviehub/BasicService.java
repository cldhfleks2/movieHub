package com.cldhfleks2.moviehub;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicService {
    @Value("/kobis.key")
    private String kobiskey;

    String getMain() {
        return "main/main";
    }

    String getDetail() {
        return "detail/detail";
    }

    //JS에서 KOBIS Key를 요청하면 String으로 전달해준다.
    ResponseEntity<String> getKobiskey() {
        return ResponseEntity.ok(kobiskey);
    }



}
