package com.cldhfleks2.moviehub.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    //알림 페이지 GET
    @GetMapping("/notification")
    String getNotification(Model model, Authentication auth) {
        return notificationService.getNotification(model, auth);
    }

    //알림 모두 읽도록 요청 : 헤더만 새로고침할거임
    @PostMapping("/api/notification/readAll")
    ResponseEntity<String> readAllNotification(Model model, Authentication auth) {
        return notificationService.readAllNotification(model, auth);
    }

}
