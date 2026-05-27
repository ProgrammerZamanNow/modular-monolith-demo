package com.khannedy.ecommerce.product;

import com.khannedy.ecommerce.product.entity.Category;
import com.khannedy.ecommerce.product.model.CategoryRequest;
import com.khannedy.ecommerce.product.model.CategoryResponse;
import com.khannedy.ecommerce.product.repository.CategoryRepository;
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
class CategoryControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        this.restTestClient = RestTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void createCategory_success() {
        restTestClient.post()
                .uri("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CategoryRequest("Electronics"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryResponse.class)
                .value(response -> {
                    assertThat(response.id()).isNotBlank();
                    assertThat(response.name()).isEqualTo("Electronics");
                });
    }

    @Test
    void getCategoryById_success() {
        Category category = new Category();
        category.setName("Clothing");
        Category savedCategory = categoryRepository.save(category);

        restTestClient.get()
                .uri("/api/v1/categories/{id}", savedCategory.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoryResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedCategory.getId());
                    assertThat(response.name()).isEqualTo("Clothing");
                });
    }

    @Test
    void getCategoryById_notFound() {
        restTestClient.get()
                .uri("/api/v1/categories/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllCategories_success() {
        Category c1 = new Category();
        c1.setName("Electronics");
        categoryRepository.save(c1);

        Category c2 = new Category();
        c2.setName("Clothing");
        categoryRepository.save(c2);

        restTestClient.get()
                .uri("/api/v1/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void updateCategory_success() {
        Category category = new Category();
        category.setName("Electronics");
        Category savedCategory = categoryRepository.save(category);

        restTestClient.put()
                .uri("/api/v1/categories/{id}", savedCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CategoryRequest("Electronics Updated"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoryResponse.class)
                .value(response -> assertThat(response.name()).isEqualTo("Electronics Updated"));
    }

    @Test
    void updateCategory_notFound() {
        restTestClient.put()
                .uri("/api/v1/categories/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CategoryRequest("Electronics Updated"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCategory_success() {
        Category category = new Category();
        category.setName("Electronics");
        Category savedCategory = categoryRepository.save(category);

        restTestClient.delete()
                .uri("/api/v1/categories/{id}", savedCategory.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(categoryRepository.existsById(savedCategory.getId())).isFalse();
    }

    @Test
    void deleteCategory_notFound() {
        restTestClient.delete()
                .uri("/api/v1/categories/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

}
