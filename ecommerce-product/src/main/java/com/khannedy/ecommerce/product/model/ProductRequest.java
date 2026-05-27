package com.khannedy.ecommerce.product.model;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String brandId,
        String categoryId
) {
}
