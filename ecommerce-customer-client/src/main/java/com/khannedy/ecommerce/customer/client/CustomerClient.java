package com.khannedy.ecommerce.customer.client;

import java.util.Optional;

public interface CustomerClient {
    
    Optional<CustomerClientResponse> getCustomer(String id);
    
}
