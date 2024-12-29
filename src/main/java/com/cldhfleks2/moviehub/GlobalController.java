package com.cldhfleks2.moviehub;

import com.cldhfleks2.moviehub.member.MemberDetail;
import com.cldhfleks2.moviehub.notification.Notification;
import com.cldhfleks2.moviehub.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {
    private final NotificationRepository notificationRepository;

    // 모든 요청에서 principal을 자동으로 모델에 추가 : 모든페이지에서 principal 사용 가능
    @ModelAttribute("principal")
    public MemberDetail addPrincipal(Authentication auth) {
        if(auth == null) return null; //인증 정보가 있을 때만
        // Authentication에서 CustomUserDetails (MemberDetail) 객체를 가져옴
        MemberDetail principal = (MemberDetail) auth.getPrincipal();
        return principal; // 모델에 principal을 추가
    }

    // 모든 페이지에서 알림정보를 출력해줌
    //notificationRepository 에서 알림가져와서 넣어줌
    @ModelAttribute
    public void addNotification(Authentication auth, Model model){
        String username = auth.getName();
        //안읽고 삭제되지않은 알림만 모두 가져오기
        List<Notification> notificationList = notificationRepository.findAllByUsernameAndIsReadAndStatus(username);
        model.addAttribute("notificationList", notificationList);
        model.addAttribute("notificationCnt", notificationList.size());
    }


}
