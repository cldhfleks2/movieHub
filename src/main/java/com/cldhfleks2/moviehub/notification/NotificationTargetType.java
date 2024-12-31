package com.cldhfleks2.moviehub.notification;

import lombok.Getter;

@Getter
public enum NotificationTargetType {
    REVIEW("영화 리뷰 게시판"), //"리뷰 게시판" 에서 변경
    COMMUNITY_REVIEW("영화 커뮤니티 댓글"),
    COMMUNITY_POST("영화 커뮤니티 게시글");

    private final String description;

    NotificationTargetType(String description) {
        this.description = description;
    }
}
