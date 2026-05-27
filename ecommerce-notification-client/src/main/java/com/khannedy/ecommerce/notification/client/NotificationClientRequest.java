package com.khannedy.ecommerce.notification.client;

public record NotificationClientRequest(
        String target,
        String message
) {
}
