package com.khannedy.ecommerce.product;

import com.khannedy.ecommerce.product.entity.Brand;
import com.khannedy.ecommerce.product.entity.Category;
import com.khannedy.ecommerce.product.entity.Product;
import com.khannedy.ecommerce.product.model.ProductRequest;
import com.khannedy.ecommerce.product.model.ProductResponse;
import com.khannedy.ecommerce.product.repository.BrandRepository;
import com.khannedy.ecommerce.product.repository.CategoryRepository;
import com.khannedy.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private RestTestClient restTestClient;

    private Brand savedBrand;
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

        Brand brand = new Brand();
        brand.setName("Nike");
        savedBrand = brandRepository.save(brand);

        Category category = new Category();
        category.setName("Footwear");
        savedCategory = categoryRepository.save(category);

        this.restTestClient = RestTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void createProduct_success() {
        ProductRequest request = new ProductRequest(
                "Air Max 90",
                "Classic running shoes",
                new BigDecimal("1500000"),
                10,
                savedBrand.getId(),
                savedCategory.getId()
        );

        restTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class)
                .value(response -> {
                    assertThat(response.id()).isNotBlank();
                    assertThat(response.name()).isEqualTo("Air Max 90");
                    assertThat(response.price()).isEqualByComparingTo(new BigDecimal("1500000"));
                    assertThat(response.stock()).isEqualTo(10);
                    assertThat(response.brand().id()).isEqualTo(savedBrand.getId());
                    assertThat(response.category().id()).isEqualTo(savedCategory.getId());
                });
    }

    @Test
    void createProduct_brandNotFound() {
        ProductRequest request = new ProductRequest(
                "Air Max 90",
                "Classic running shoes",
                new BigDecimal("1500000"),
                10,
                "non-existent-brand-id",
                savedCategory.getId()
        );

        restTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getProductById_success() {
        Product product = new Product();
        product.setName("Air Max 90");
        product.setDescription("Classic running shoes");
        product.setPrice(new BigDecimal("1500000"));
        product.setStock(10);
        product.setBrand(savedBrand);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);

        restTestClient.get()
                .uri("/api/v1/products/{id}", savedProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedProduct.getId());
                    assertThat(response.name()).isEqualTo("Air Max 90");
                    assertThat(response.brand().name()).isEqualTo("Nike");
                    assertThat(response.category().name()).isEqualTo("Footwear");
                });
    }

    @Test
    void getProductById_notFound() {
        restTestClient.get()
                .uri("/api/v1/products/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllProducts_success() {
        Product p1 = new Product();
        p1.setName("Air Max 90");
        p1.setDescription("Running shoes");
        p1.setPrice(new BigDecimal("1500000"));
        p1.setStock(10);
        p1.setBrand(savedBrand);
        p1.setCategory(savedCategory);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Air Force 1");
        p2.setDescription("Casual shoes");
        p2.setPrice(new BigDecimal("1200000"));
        p2.setStock(5);
        p2.setBrand(savedBrand);
        p2.setCategory(savedCategory);
        productRepository.save(p2);

        restTestClient.get()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void updateProduct_success() {
        Product product = new Product();
        product.setName("Air Max 90");
        product.setDescription("Classic running shoes");
        product.setPrice(new BigDecimal("1500000"));
        product.setStock(10);
        product.setBrand(savedBrand);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);

        ProductRequest request = new ProductRequest(
                "Air Max 90 Updated",
                "Updated description",
                new BigDecimal("1800000"),
                20,
                savedBrand.getId(),
                savedCategory.getId()
        );

        restTestClient.put()
                .uri("/api/v1/products/{id}", savedProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .value(response -> {
                    assertThat(response.name()).isEqualTo("Air Max 90 Updated");
                    assertThat(response.price()).isEqualByComparingTo(new BigDecimal("1800000"));
                    assertThat(response.stock()).isEqualTo(20);
                });
    }

    @Test
    void deleteProduct_success() {
        Product product = new Product();
        product.setName("Air Max 90");
        product.setDescription("Classic running shoes");
        product.setPrice(new BigDecimal("1500000"));
        product.setStock(10);
        product.setBrand(savedBrand);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);

        restTestClient.delete()
                .uri("/api/v1/products/{id}", savedProduct.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(productRepository.existsById(savedProduct.getId())).isFalse();
    }

    @Test
    void deleteProduct_notFound() {
        restTestClient.delete()
                .uri("/api/v1/products/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

}
