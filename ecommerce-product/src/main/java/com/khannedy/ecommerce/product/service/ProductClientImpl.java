package com.khannedy.ecommerce.product.service;

import com.khannedy.ecommerce.product.client.ProductClient;
import com.khannedy.ecommerce.product.client.ProductClientResponse;
import com.khannedy.ecommerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductClientImpl implements ProductClient {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductClientImpl(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductClientResponse> getProduct(String id) {
        return productRepository.findById(id)
                .map(product -> new ProductClientResponse(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock()
                ));
    }

    @Override
    public void reduceStock(String id, Integer quantity) {
        productService.reduceStock(id, quantity);
    }

    @Override
    public void restoreStock(String id, Integer quantity) {
        productService.restoreStock(id, quantity);
    }
}
