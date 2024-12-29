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
    String getNotification(Model model, Authentication auth, Integer pageIdx) {
        return notificationService.getNotification(model, auth, pageIdx);
    }

    //알림 모두 읽도록 요청
    @PostMapping("/api/notification/readAll")
    ResponseEntity<String> readAllNotification(Authentication auth) {
        return notificationService.readAllNotification(auth);
    }

    //알림 하나 읽도록 요청
    @PostMapping("/api/notification/read")
    ResponseEntity<String> readNotification(Long notificationId, Authentication auth) {
        return notificationService.readNotification(notificationId, auth);
    }


}
