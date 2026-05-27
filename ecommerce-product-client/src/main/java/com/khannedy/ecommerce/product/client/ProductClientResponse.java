package com.khannedy.ecommerce.product.client;

import java.math.BigDecimal;

public record ProductClientResponse(
        String id,
        String name,
        BigDecimal price,
        Integer stock
) {
}
