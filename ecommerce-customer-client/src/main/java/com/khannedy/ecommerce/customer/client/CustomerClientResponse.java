package com.khannedy.ecommerce.customer.client;

public record CustomerClientResponse(
        String id,
        String name,
        String email,
        String phone
) {
}
