package com.khannedy.ecommerce.order;

import com.khannedy.ecommerce.customer.entity.Customer;
import com.khannedy.ecommerce.customer.repository.CustomerRepository;
import com.khannedy.ecommerce.order.entity.Order;
import com.khannedy.ecommerce.order.model.OrderItemRequest;
import com.khannedy.ecommerce.order.model.OrderRequest;
import com.khannedy.ecommerce.order.model.OrderResponse;
import com.khannedy.ecommerce.order.repository.OrderItemRepository;
import com.khannedy.ecommerce.order.repository.OrderRepository;
import com.khannedy.ecommerce.payment.repository.PaymentRepository;
import com.khannedy.ecommerce.product.entity.Brand;
import com.khannedy.ecommerce.product.entity.Category;
import com.khannedy.ecommerce.product.entity.Product;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private RestTestClient restTestClient;

    private Product savedProduct;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        paymentRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

        Brand brand = new Brand();
        brand.setName("Nike");
        Brand savedBrand = brandRepository.save(brand);

        Category category = new Category();
        category.setName("Footwear");
        Category savedCategory = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Air Max");
        product.setDescription("Shoes");
        product.setPrice(new BigDecimal("1000000"));
        product.setStock(10);
        product.setBrand(savedBrand);
        product.setCategory(savedCategory);
        savedProduct = productRepository.save(product);

        Customer customer = new Customer();
        customer.setId(java.util.UUID.randomUUID().toString());
        customer.setName("Eko");
        customer.setEmail("eko@example.com");
        customer.setPhone("08123456789");
        savedCustomer = customerRepository.save(customer);

        this.restTestClient = RestTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void createOrder_success() {
        OrderRequest request = new OrderRequest(
                savedCustomer.getId(),
                List.of(new OrderItemRequest(savedProduct.getId(), 2))
        );

        restTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .value(response -> {
                    assertThat(response.id()).isNotBlank();
                    assertThat(response.customerId()).isEqualTo(savedCustomer.getId());
                    assertThat(response.customerName()).isEqualTo(savedCustomer.getName());
                    assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
                    assertThat(response.paymentId()).isNotNull();
                    assertThat(response.status()).isEqualTo("PAID");
                    assertThat(response.items()).hasSize(1);
                    assertThat(response.items().get(0).productId()).isEqualTo(savedProduct.getId());
                });
                
        // Verify it was saved
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getPaymentId()).isNotNull();
        assertThat(orders.get(0).getStatus()).isEqualTo("PAID");

        // Verify product stock reduced
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(8); // 10 - 2
    }

    @Test
    void createOrder_productNotFound() {
        OrderRequest request = new OrderRequest(
                savedCustomer.getId(),
                List.of(new OrderItemRequest("invalid-product-id", 2))
        );

        restTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createOrder_insufficientStock() {
        OrderRequest request = new OrderRequest(
                savedCustomer.getId(),
                List.of(new OrderItemRequest(savedProduct.getId(), 20)) // Stock is only 10
        );

        restTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getOrderById_success() {
        // Create order via API first to reuse setup logic
        OrderRequest request = new OrderRequest(
                savedCustomer.getId(),
                List.of(new OrderItemRequest(savedProduct.getId(), 1))
        );

        OrderResponse createdResponse = restTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .returnResult().getResponseBody();

        // Now test the GET endpoint
        restTestClient.get()
                .uri("/api/v1/orders/{id}", createdResponse.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(createdResponse.id());
                    assertThat(response.customerId()).isEqualTo(savedCustomer.getId());
                    assertThat(response.customerName()).isEqualTo(savedCustomer.getName());
                    assertThat(response.paymentId()).isEqualTo(createdResponse.paymentId());
                });
    }

    @Test
    void getOrderById_notFound() {
        restTestClient.get()
                .uri("/api/v1/orders/{id}", "invalid-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void cancelOrder_success() {
        OrderRequest request = new OrderRequest(
                savedCustomer.getId(),
                List.of(new OrderItemRequest(savedProduct.getId(), 2))
        );

        OrderResponse createdResponse = restTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .returnResult().getResponseBody();

        // Verify stock reduced first
        Product productAfterCreate = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(productAfterCreate.getStock()).isEqualTo(8);

        // Cancel order
        restTestClient.post()
                .uri("/api/v1/orders/{id}/cancel", createdResponse.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(createdResponse.id());
                    assertThat(response.status()).isEqualTo("CANCELED");
                });

        // Verify stock restored
        Product productAfterCancel = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(productAfterCancel.getStock()).isEqualTo(10);

        // Verify payment canceled
        Order canceledOrder = orderRepository.findById(createdResponse.id()).orElseThrow();
        com.khannedy.ecommerce.payment.entity.Payment payment = paymentRepository.findById(canceledOrder.getPaymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo("CANCELED");
    }
}
