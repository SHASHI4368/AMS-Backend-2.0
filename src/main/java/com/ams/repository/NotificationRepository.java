package com.ams.repository;

import com.ams.entity.Notification;
import com.ams.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUser(User user, Pageable pageable);
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
}
