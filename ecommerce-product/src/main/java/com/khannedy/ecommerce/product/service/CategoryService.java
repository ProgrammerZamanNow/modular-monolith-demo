package com.khannedy.ecommerce.product.service;

import com.khannedy.ecommerce.product.entity.Category;
import com.khannedy.ecommerce.product.model.CategoryRequest;
import com.khannedy.ecommerce.product.model.CategoryResponse;
import com.khannedy.ecommerce.product.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(String id) {
        Category category = findByIdOrThrow(id);
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse update(String id, CategoryRequest request) {
        Category category = findByIdOrThrow(id);
        category.setName(request.name());
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    public void delete(String id) {
        Category category = findByIdOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findByIdOrThrow(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

}
