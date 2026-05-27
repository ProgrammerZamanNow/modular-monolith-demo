package com.khannedy.ecommerce.notification.service;

import com.khannedy.ecommerce.notification.client.NotificationClient;
import com.khannedy.ecommerce.notification.client.NotificationClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.khannedy.ecommerce.notification.event.NotificationEvent;

@Service
public class NotificationClientImpl implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientImpl.class);

    private final ApplicationEventPublisher eventPublisher;

    public NotificationClientImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void sendNotification(NotificationClientRequest request) {
        log.info("Publishing notification event for target [{}]: {}", request.target(), request.message());
        eventPublisher.publishEvent(new NotificationEvent(request.target(), request.message()));
    }
}
