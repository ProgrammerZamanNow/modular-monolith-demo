package com.khannedy.ecommerce.product;

import com.khannedy.ecommerce.product.entity.Brand;
import com.khannedy.ecommerce.product.model.BrandRequest;
import com.khannedy.ecommerce.product.model.BrandResponse;
import com.khannedy.ecommerce.product.repository.BrandRepository;
import com.khannedy.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BrandControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        brandRepository.deleteAll();
        this.restTestClient = RestTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void createBrand_success() {
        restTestClient.post()
                .uri("/api/v1/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BrandRequest("Nike"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BrandResponse.class)
                .value(response -> {
                    assertThat(response.id()).isNotBlank();
                    assertThat(response.name()).isEqualTo("Nike");
                });
    }

    @Test
    void getBrandById_success() {
        Brand brand = new Brand();
        brand.setName("Adidas");
        Brand savedBrand = brandRepository.save(brand);

        restTestClient.get()
                .uri("/api/v1/brands/{id}", savedBrand.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BrandResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedBrand.getId());
                    assertThat(response.name()).isEqualTo("Adidas");
                });
    }

    @Test
    void getBrandById_notFound() {
        restTestClient.get()
                .uri("/api/v1/brands/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllBrands_success() {
        Brand b1 = new Brand();
        b1.setName("Nike");
        brandRepository.save(b1);

        Brand b2 = new Brand();
        b2.setName("Adidas");
        brandRepository.save(b2);

        restTestClient.get()
                .uri("/api/v1/brands")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void updateBrand_success() {
        Brand brand = new Brand();
        brand.setName("Nike");
        Brand savedBrand = brandRepository.save(brand);

        restTestClient.put()
                .uri("/api/v1/brands/{id}", savedBrand.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BrandRequest("Nike Updated"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BrandResponse.class)
                .value(response -> assertThat(response.name()).isEqualTo("Nike Updated"));
    }

    @Test
    void updateBrand_notFound() {
        restTestClient.put()
                .uri("/api/v1/brands/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BrandRequest("Nike Updated"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteBrand_success() {
        Brand brand = new Brand();
        brand.setName("Nike");
        Brand savedBrand = brandRepository.save(brand);

        restTestClient.delete()
                .uri("/api/v1/brands/{id}", savedBrand.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(brandRepository.existsById(savedBrand.getId())).isFalse();
    }

    @Test
    void deleteBrand_notFound() {
        restTestClient.delete()
                .uri("/api/v1/brands/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

}
