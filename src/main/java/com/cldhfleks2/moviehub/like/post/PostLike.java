package com.cldhfleks2.moviehub.like.post;

import com.cldhfleks2.moviehub.community.Post;
import com.cldhfleks2.moviehub.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE post_like SET status = 0 WHERE id = ?")
@ToString
@NoArgsConstructor
public class PostLike {
    @ToString.Exclude
    @ManyToOne
    private Post post; //댓글

    @ToString.Exclude
    @ManyToOne
    private Member sender; //좋아요 누른 사람

    @Builder(builderMethodName = "create")
    public PostLike(Post post, Member sender) {
        this.post = post;
        this.sender = sender;
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
