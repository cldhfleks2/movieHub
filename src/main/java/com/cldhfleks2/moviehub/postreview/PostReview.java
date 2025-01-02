package com.cldhfleks2.moviehub.postreview;

import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE post_review SET status = 0 WHERE id = ?")
@ToString
public class PostReview {
    private String content; //리뷰 내용

    @ToString.Exclude
    @ManyToOne
    private Member member; //댓글 작성 유저

    @ToString.Exclude
    @ManyToOne
    private Post post; //게시글

    @ToString.Exclude
    @ManyToOne
    private PostReview parent; //부모 댓글

    @Column(nullable = false)
    @Getter @Setter
    private int depth = 0; //댓글 깊이 : 답글의 답글은 제한하기위함

    @PrePersist
    public void prePersist() {
        if (parent != null) { // 부모 댓글이 있으면 부모의 depth + 1을 설정
            this.depth = parent.getDepth() + 1;
        }
    }

    //아래는 기본 필드들
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;
    @UpdateTimestamp
    private LocalDateTime updateDate;
    private int status = 1; //존재 여부 (1:보임 0:삭제)
}
