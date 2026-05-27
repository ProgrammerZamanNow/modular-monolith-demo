package com.khannedy.ecommerce.notification.repository;

import com.khannedy.ecommerce.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
}
