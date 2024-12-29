package com.cldhfleks2.moviehub.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    //알림 페이지 GET
    String getNotification(Model model, Authentication auth) {
        return "notification/notification";
    }

    //알림 모두 읽도록 요청
    String readAllNotification(Model model, Authentication auth) {


        return "header/header";
    }
}
