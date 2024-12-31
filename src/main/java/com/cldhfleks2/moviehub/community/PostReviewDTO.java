package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class PostReviewDTO {
    private String content;
    private Long parentId;
    private Long postId;

    private Member member; //댓글 작성자 : 댓글리스트에서 사용
    private LocalDateTime updateDate; //댓글 작성날짜 : 댓글리스트에서 사용
    private Long likeCount; //댓글 좋아요 갯수  : 댓글리스트에서 사용
    private Boolean isAuthor; // 작성자 본인인지 여부 :

    private Long id; //현재 댓글 id
    private int depth; // 댓글 depth : 게시물 상세페이지
    private List<PostReviewDTO> children; // 자식 댓글 리스트 : 게시물 상세페이지
    
    private Boolean isLiked; //좋아요 한 상태인지
}
