package com.khannedy.ecommerce.notification.service;

import tools.jackson.databind.ObjectMapper;
import com.khannedy.ecommerce.notification.client.NotificationClientRequest;
import com.khannedy.ecommerce.notification.entity.Notification;
import com.khannedy.ecommerce.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "notification_events", groupId = "notification-group")
    @Transactional
    public void handleNotificationEvent(String payload) {
        try {
            NotificationClientRequest request = objectMapper.readValue(payload, NotificationClientRequest.class);
            log.info("Received Kafka notification event for target: {}", request.target());
            
            Notification notification = new Notification();
            notification.setTarget(request.target());
            notification.setMessage(request.message());
            
            notificationRepository.save(notification);
            
            log.info("Saved notification to database with ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to process notification payload: {}", payload, e);
        }
    }
}
