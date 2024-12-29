package com.cldhfleks2.moviehub.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    COMMENT_ADDED("댓글 작성"),
    LIKE_RECEIVED("좋아요"),
    MESSAGE_RECEIVED("쪽지를 보냄");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
