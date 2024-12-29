package com.cldhfleks2.moviehub.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    //username으로 isRead=1이고 Status=1인 Notification리스트를 찾기
    @Query("SELECT n FROM Notification n WHERE n.receiver.username = :username AND n.isRead = 1 AND n.status = 1")
    Page<Notification> findAllByUsernameAndIsReadAndStatus(String username, Pageable pageable);


    //receiver의 username에 해당하는 모든 알림의 isRead를 0으로 업데이트 : status=1인것들만 업데이트함
    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.isRead = 0 WHERE n.receiver.username = :receiverUsername AND n.isRead = 1 AND n.status = 1")
    void updateNotificationByUsernameAndStatus(String receiverUsername);
}
