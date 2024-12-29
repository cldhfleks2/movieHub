package com.cldhfleks2.moviehub.notification;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    String getNotification(Model model, Authentication auth, Integer pageIdx) {
        if(pageIdx == null) pageIdx = 1;
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/notification", "유저 정보를 찾을 수 없습니다.", String.class);

        //한페이지에 10개씩 보여줄것
        Page<Notification> notificationPage = notificationRepository.findAllByUsernameAndIsReadAndStatus(username, PageRequest.of(pageIdx-1, 10));
        model.addAttribute("notificationPage", notificationPage); //10개만 보여줌
        //헤더에서 모든 페이지에 불러온값. 지우고싶으면 0이나 null등을 보내자.
        //이미 알림페이지에서는 해당하는 태그를 hide()해놔서
        //상관없긴해
        //model.addAttribute("notificationList", notificationPage.getContent());
//        model.addAttribute("notificationCnt", notificationPage.getTotalElements());

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

}
