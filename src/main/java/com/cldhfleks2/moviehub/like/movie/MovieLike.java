package com.cldhfleks2.moviehub.like.movie;

import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.movie.Movie;
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
@SQLDelete(sql = "UPDATE movie_like SET status = 0 WHERE id = ?")
@ToString
public class MovieLike {
    @ToString.Exclude
    @ManyToOne
    private Movie movie;

    @ToString.Exclude
    @ManyToOne
    private Member member;

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
