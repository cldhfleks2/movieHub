package com.cldhfleks2.moviehub.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
}
