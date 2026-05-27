package com.khannedy.ecommerce.payment.client;

public record PaymentClientResponse(
        String paymentId,
        String status
) {
}
