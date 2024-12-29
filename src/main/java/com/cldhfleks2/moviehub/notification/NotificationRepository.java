package com.cldhfleks2.moviehub.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    //username으로 isRead=1이고 Status=1인 Notification리스트를 찾기
    @Query("SELECT n FROM Notification n WHERE n.receiver.username = :username AND n.isRead = 1 AND n.status = 1")
    List<Notification> findAllByUsernameAndIsReadAndStatus(String username);

}
