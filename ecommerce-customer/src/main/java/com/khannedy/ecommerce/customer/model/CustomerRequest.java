package com.khannedy.ecommerce.customer.model;

public record CustomerRequest(
        String name,
        String email,
        String phone
) {
}
