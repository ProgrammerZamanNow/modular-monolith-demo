package com.khannedy.ecommerce.notification.event;

public record NotificationEvent(
        String target,
        String message
) {
}
