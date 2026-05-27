package com.khannedy.ecommerce.product.service;

import com.khannedy.ecommerce.product.entity.Brand;
import com.khannedy.ecommerce.product.model.BrandRequest;
import com.khannedy.ecommerce.product.model.BrandResponse;
import com.khannedy.ecommerce.product.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public BrandResponse create(BrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.name());
        brand = brandRepository.save(brand);
        return toResponse(brand);
    }

    @Transactional(readOnly = true)
    public BrandResponse getById(String id) {
        Brand brand = findByIdOrThrow(id);
        return toResponse(brand);
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BrandResponse update(String id, BrandRequest request) {
        Brand brand = findByIdOrThrow(id);
        brand.setName(request.name());
        brand = brandRepository.save(brand);
        return toResponse(brand);
    }

    public void delete(String id) {
        Brand brand = findByIdOrThrow(id);
        brandRepository.delete(brand);
    }

    private Brand findByIdOrThrow(String id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + id));
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }

}
