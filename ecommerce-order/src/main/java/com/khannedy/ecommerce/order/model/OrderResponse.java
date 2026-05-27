package com.khannedy.ecommerce.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String customerId,
        String customerName,
        BigDecimal totalAmount,
        String paymentId,
        String status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
