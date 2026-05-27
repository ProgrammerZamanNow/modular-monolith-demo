package com.khannedy.ecommerce.order.model;

public record OrderItemRequest(
        String productId,
        Integer quantity
) {
}
