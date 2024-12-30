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
}
