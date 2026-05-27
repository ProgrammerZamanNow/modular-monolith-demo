package com.khannedy.ecommerce.product.client;

import java.util.Optional;

public interface ProductClient {
    
    Optional<ProductClientResponse> getProduct(String id);
    
    void reduceStock(String id, Integer quantity);
    
    void restoreStock(String id, Integer quantity);
    
}
