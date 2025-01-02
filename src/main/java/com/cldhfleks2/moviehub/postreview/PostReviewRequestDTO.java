package com.cldhfleks2.moviehub.postreview;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class PostReviewRequestDTO { //js에서 서버로 전달용 DTO
    private String content;
    private Long parentId; // 부모 댓글 ID (없으면 null)
    private Long postId; // 댓글 처음 작성 할때 사용 html -> service 보낼때  : 댓글작성요청 ajax-js에서 사용
}