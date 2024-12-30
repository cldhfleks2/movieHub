package com.cldhfleks2.moviehub.community;

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
@SQLDelete(sql = "UPDATE post SET status = 0, update_date = CURRENT_TIMESTAMP WHERE id = ?")
@ToString
public class Post {
    private String title; //제목
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; //내용
    private Long view=0L; //조회수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType; //게시판 종류 : 문자열로 저장됨 => "토론 게시판"

    @ToString.Exclude
    @ManyToOne
    private Member member; //게시글 작성 유저

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
