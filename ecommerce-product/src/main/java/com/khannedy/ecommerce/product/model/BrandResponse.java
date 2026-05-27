package com.khannedy.ecommerce.product.model;

import java.time.LocalDateTime;

public record BrandResponse(
        String id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
