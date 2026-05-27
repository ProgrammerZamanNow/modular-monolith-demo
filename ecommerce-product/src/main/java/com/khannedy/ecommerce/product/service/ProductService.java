package com.khannedy.ecommerce.product.service;

import com.khannedy.ecommerce.product.entity.Brand;
import com.khannedy.ecommerce.product.entity.Category;
import com.khannedy.ecommerce.product.entity.Product;
import com.khannedy.ecommerce.product.model.BrandResponse;
import com.khannedy.ecommerce.product.model.CategoryResponse;
import com.khannedy.ecommerce.product.model.ProductRequest;
import com.khannedy.ecommerce.product.model.ProductResponse;
import com.khannedy.ecommerce.product.repository.BrandRepository;
import com.khannedy.ecommerce.product.repository.CategoryRepository;
import com.khannedy.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public ProductResponse create(ProductRequest request) {
        Brand brand = findBrandOrThrow(request.brandId());
        Category category = findCategoryOrThrow(request.categoryId());

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setBrand(brand);
        product.setCategory(category);

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(String id) {
        Product product = findByIdOrThrow(id);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse update(String id, ProductRequest request) {
        Product product = findByIdOrThrow(id);
        Brand brand = findBrandOrThrow(request.brandId());
        Category category = findCategoryOrThrow(request.categoryId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setBrand(brand);
        product.setCategory(category);

        product = productRepository.save(product);
        return toResponse(product);
    }

    public void delete(String id) {
        Product product = findByIdOrThrow(id);
        productRepository.delete(product);
    }

    public void reduceStock(String id, Integer quantity) {
        Product product = findByIdOrThrow(id);
        if (product.getStock() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    public void restoreStock(String id, Integer quantity) {
        Product product = findByIdOrThrow(id);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    private Product findByIdOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));
    }

    private Brand findBrandOrThrow(String brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + brandId));
    }

    private Category findCategoryOrThrow(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + categoryId));
    }

    private ProductResponse toResponse(Product product) {
        BrandResponse brandResponse = new BrandResponse(
                product.getBrand().getId(),
                product.getBrand().getName(),
                product.getBrand().getCreatedAt(),
                product.getBrand().getUpdatedAt()
        );
        CategoryResponse categoryResponse = new CategoryResponse(
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getCreatedAt(),
                product.getCategory().getUpdatedAt()
        );
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                brandResponse,
                categoryResponse,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

}
