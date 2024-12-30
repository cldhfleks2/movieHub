package com.cldhfleks2.moviehub.community;

import lombok.Getter;

@Getter
public enum PostType {
    FREE("자유 게시판"),
    NEWS("소식 게시판"),
    DISCUSSION("토론 게시판");

    private final String description;

    PostType(String description) {
        this.description = description;
    }
}
