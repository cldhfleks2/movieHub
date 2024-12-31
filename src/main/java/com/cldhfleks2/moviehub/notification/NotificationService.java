package com.cldhfleks2.moviehub.notification;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;

    public NotificationDTO getNotificationDTO(Notification notification) {
        String URL = "#"; //DTO에 추가된 사항
        if(notification.getNotificationType() == NotificationType.LIKE_RECEIVED && //영화 리뷰 게시판에 좋아요 한경우
            notification.getTargetType() == NotificationTargetType.REVIEW){
            URL = "/movieReview"; //영화 리뷰 게시판으로 이동
        }else if(notification.getNotificationType() == NotificationType.COMMENT_ADDED && notification.getTargetType() ==NotificationTargetType.COMMUNITY){
            URL = "/postDetail/" + notification.getTargetId(); //게시물로 이동
        }


        return NotificationDTO.create()
                .id(notification.getId())
                .receiver(notification.getReceiver())
                .sender(notification.getSender())
                .notificationType(notification.getNotificationType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .updateDate(notification.getUpdateDate())
                .URL(URL)
                .build();
    }

    //알림 페이지 GET
    String getNotification(Model model, Authentication auth, Integer pageIdx) {
        if(pageIdx == null) pageIdx = 1;
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/notification", "유저 정보를 찾을 수 없습니다.", String.class);

        //한페이지에 10개씩 보여줄것
        Page<Notification> notificationPage = notificationRepository.findAllByUsernameAndIsReadAndStatus(username, PageRequest.of(pageIdx-1, 10));
        List<NotificationDTO> notificationDTOList = new ArrayList<>();
        for(Notification notification : notificationPage) {
            NotificationDTO notificationDTO = getNotificationDTO(notification);
            notificationDTOList.add(notificationDTO);
        }
        Page<NotificationDTO> notificationDTOPage = new PageImpl<>(
                notificationDTOList,
                notificationPage.getPageable(),
                notificationPage.getTotalElements()
        );

        //DTO를 만들어서 전달.
        model.addAttribute("notificationDTOPage", notificationDTOPage); //10개만 보여줌
        
        return "notification/notification";
    }

    //알림 모두 읽도록 요청
    ResponseEntity<String> readAllNotification(Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/notification/readAll", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        //해당 유저의 모든 알림을 읽음 처리로 업데이트
        notificationRepository.updateNotificationByUsernameAndStatus(username);

        return ResponseEntity.status(HttpStatus.OK.value()).build();
    }

    //알림 하나 읽도록 요청
    ResponseEntity<String> readNotification(Long notificationId, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/notification/read", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Notification> notificationObj = notificationRepository.findById(notificationId);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/notification/read", "알림을 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Notification notification = notificationObj.get();
        if(notification.getReceiver().getId() != member.getId()) //본인의 알림이 아닐경우 삭제 불가
            return ErrorService.send(HttpStatus.FORBIDDEN.value(), "/api/notification/read", "본인의 알림이 아닙니다.", ResponseEntity.class);

        notification.setIsRead(0); //알림을 읽음 처리
        notificationRepository.save(notification);

        return ResponseEntity.status(HttpStatus.OK.value()).build();
    }

}
