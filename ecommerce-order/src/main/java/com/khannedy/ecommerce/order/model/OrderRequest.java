package com.khannedy.ecommerce.order.model;

import java.util.List;

public record OrderRequest(
        String customerId,
        List<OrderItemRequest> items
) {
}
