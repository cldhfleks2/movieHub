package com.cldhfleks2.moviehub.notification;

import lombok.Getter;

@Getter
public enum TargetType {
    REVIEW("리뷰"),
    DISCUSSION("토론"),
    COMMENT("댓글");

    private final String description;

    TargetType(String description) {
        this.description = description;
    }
}
