package com.khannedy.ecommerce.customer;

import com.khannedy.ecommerce.customer.entity.Customer;
import com.khannedy.ecommerce.customer.model.CustomerRequest;
import com.khannedy.ecommerce.customer.model.CustomerResponse;
import com.khannedy.ecommerce.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CustomerRepository customerRepository;

    private RestTestClient restTestClient;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("08111222333");
        savedCustomer = customerRepository.save(customer);

        this.restTestClient = RestTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void create_success() {
        CustomerRequest request = new CustomerRequest("Jane Doe", "jane@example.com", "08999888777");

        restTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .value(response -> {
                    assertThat(response.id()).isNotBlank();
                    assertThat(response.name()).isEqualTo("Jane Doe");
                    assertThat(response.email()).isEqualTo("jane@example.com");
                    assertThat(response.phone()).isEqualTo("08999888777");
                });
    }

    @Test
    void get_success() {
        restTestClient.get()
                .uri("/api/v1/customers/{id}", savedCustomer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedCustomer.getId());
                    assertThat(response.name()).isEqualTo("John Doe");
                    assertThat(response.email()).isEqualTo("john@example.com");
                });
    }

    @Test
    void get_notFound() {
        restTestClient.get()
                .uri("/api/v1/customers/{id}", "not-found")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_success() {
        CustomerRequest request = new CustomerRequest("John Updated", "john2@example.com", "0000");

        restTestClient.put()
                .uri("/api/v1/customers/{id}", savedCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedCustomer.getId());
                    assertThat(response.name()).isEqualTo("John Updated");
                    assertThat(response.email()).isEqualTo("john2@example.com");
                    assertThat(response.phone()).isEqualTo("0000");
                });
    }

    @Test
    void delete_success() {
        restTestClient.delete()
                .uri("/api/v1/customers/{id}", savedCustomer.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(customerRepository.findById(savedCustomer.getId())).isEmpty();
    }

    @Test
    void getAll_success() {
        restTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CustomerResponse>>() {})
                .value(responses -> {
                    assertThat(responses).hasSize(1);
                    assertThat(responses.get(0).id()).isEqualTo(savedCustomer.getId());
                });
    }
}
