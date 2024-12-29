package com.cldhfleks2.moviehub.notification;

import com.cldhfleks2.moviehub.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(builderMethodName = "create")
public class NotificationDTO {
    private Member receiver; //받는 사람
    private Member sender; //보낸 사람 (댓글작성자, 좋아요 누른사람등..)

    private NotificationType notificationType; //알림 종류
    private NotificationTargetType targetType; //알림이 발생한 타겟
    private Long targetId;
    private String message;
    private int isRead;

    private LocalDateTime updateDate;

    private String URL; //알림종류에 따른 타겟 위치 url
}
