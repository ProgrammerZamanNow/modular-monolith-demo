package com.khannedy.ecommerce.customer.model;

import java.time.LocalDateTime;

public record CustomerResponse(
        String id,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
