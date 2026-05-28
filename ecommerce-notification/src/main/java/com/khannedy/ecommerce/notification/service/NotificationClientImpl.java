package com.khannedy.ecommerce.notification.service;

import com.khannedy.ecommerce.notification.client.NotificationClient;
import com.khannedy.ecommerce.notification.client.NotificationClientRequest;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationClientImpl implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientImpl.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationClientImpl(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendNotification(NotificationClientRequest request) {
        try {
            log.info("Publishing notification to Kafka for target [{}]: {}", request.target(), request.message());
            String payload = objectMapper.writeValueAsString(request);
            kafkaTemplate.send("notification_events", payload);
        } catch (Exception e) {
            log.error("Failed to serialize notification request", e);
        }
    }
}
