package com.khannedy.ecommerce.product.model;

import java.time.LocalDateTime;

public record CategoryResponse(
        String id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
