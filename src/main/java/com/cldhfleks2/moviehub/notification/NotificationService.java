package com.cldhfleks2.moviehub.notification;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    //알림 페이지 GET
    String getNotification(Model model, Authentication auth) {
        return "notification/notification";
    }

    //알림 모두 읽도록 요청
    ResponseEntity<String> readAllNotification(Model model, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/notification/readAll", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        //해당 유저의 모든 알림을 읽음 처리로 업데이트
        notificationRepository.updateNotificationByUsernameAndStatus(username);

        return ResponseEntity.status(HttpStatus.OK.value()).build();
    }

    //알림 페이지 GET에 쓸것
    //        model.addAttribute("notificationList", notificationPage.getContent()); //10개만 보여줌
//        model.addAttribute("notificationCnt", notificationPage.getTotalElements());

}
