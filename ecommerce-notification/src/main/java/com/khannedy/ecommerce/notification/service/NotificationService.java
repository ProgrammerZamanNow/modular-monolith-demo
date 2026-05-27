package com.khannedy.ecommerce.notification.service;

import com.khannedy.ecommerce.notification.entity.Notification;
import com.khannedy.ecommerce.notification.event.NotificationEvent;
import com.khannedy.ecommerce.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event for target: {}", event.target());
        
        Notification notification = new Notification();
        notification.setTarget(event.target());
        notification.setMessage(event.message());
        
        notificationRepository.save(notification);
        
        log.info("Saved notification to database with ID: {}", notification.getId());
    }
}
