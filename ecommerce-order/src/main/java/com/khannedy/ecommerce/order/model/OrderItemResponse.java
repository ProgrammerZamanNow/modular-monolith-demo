package com.khannedy.ecommerce.order.model;

import java.math.BigDecimal;

public record OrderItemResponse(
        String id,
        String productId,
        BigDecimal price,
        Integer quantity
) {
}
