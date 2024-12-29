package com.cldhfleks2.moviehub.notification;

import lombok.Getter;

@Getter
public enum NotificationTargetType {
    REVIEW("리뷰 게시판"),
    DISCUSSION("토론 게시판"),
    COMMENT("유저 프로필");

    private final String description;

    NotificationTargetType(String description) {
        this.description = description;
    }
}
