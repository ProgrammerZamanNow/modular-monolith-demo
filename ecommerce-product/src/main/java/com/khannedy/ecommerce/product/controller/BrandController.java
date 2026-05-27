package com.khannedy.ecommerce.product.controller;

import com.khannedy.ecommerce.product.model.BrandRequest;
import com.khannedy.ecommerce.product.model.BrandResponse;
import com.khannedy.ecommerce.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@RequestBody BrandRequest request) {
        return brandService.create(request);
    }

    @GetMapping
    public List<BrandResponse> getAll() {
        return brandService.getAll();
    }

    @GetMapping("/{id}")
    public BrandResponse getById(@PathVariable String id) {
        return brandService.getById(id);
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable String id, @RequestBody BrandRequest request) {
        return brandService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        brandService.delete(id);
    }

}
