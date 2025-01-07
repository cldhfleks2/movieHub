package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class PostDTO {
    private PostType postType;
    private String title;
    private String content;
    private Long view; //조회수 : postDetail
    private Member member; //작성자 : postDetail
    private LocalDateTime updateDate; //최근시간 : postDetail
    private Long postId; //postDetail 사용
    private Boolean isAuthor; //글 제작자와 auth가 동일하면 : postDetail

    private Long likeCount; //게시글 좋아요 갯수 : community
    private Long reviewCount; //댓글 갯수 : community
}
