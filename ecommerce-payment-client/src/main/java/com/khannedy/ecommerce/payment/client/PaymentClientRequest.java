package com.khannedy.ecommerce.payment.client;

import java.math.BigDecimal;

public record PaymentClientRequest(
        String orderId,
        BigDecimal amount
) {
}
