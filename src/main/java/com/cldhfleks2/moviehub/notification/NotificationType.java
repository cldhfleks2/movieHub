package com.cldhfleks2.moviehub.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    COMMENT_ADDED("리뷰에 댓글 작성"),
    LIKE_RECEIVED("리뷰 좋아요"),
    MESSAGE_RECEIVED("좋아요");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
