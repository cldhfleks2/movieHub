package com.cldhfleks2.moviehub.notification;

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
@SQLDelete(sql = "UPDATE notification SET status = 0 WHERE id = ?")
@ToString
@NoArgsConstructor //기본 생성자 자동생성 : builder를 사용해서 필요함
public class Notification {
    @ToString.Exclude
    @ManyToOne
    private Member receiver; //받는 사람

    @ToString.Exclude
    @ManyToOne
    private Member sender; //보낸 사람 (댓글작성자, 좋아요 누른사람등..)

    //알림종류 + 타겟 => 정확한 알림위치를 알 수 있음
    //예) 댓글작성 + 리뷰 게시판 => 리뷰게시판에 댓글작성
    //예) 댓글작성 + 토론 게시판 => 토론게시판에 댓글작성
    //확장성이 좋음
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType; //알림 종류
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType targetType; //알림이 발생한 타겟

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private int isRead = 1; //확인 여부 : 기본값 안읽음

    @Builder(builderMethodName = "create")
    public Notification(Member receiver, Member sender,
                        NotificationType notificationType, NotificationTargetType targetType,
                        Long targetId, String message) {
        this.receiver = receiver;
        this.sender = sender;
        this.notificationType = notificationType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.message = message;
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
