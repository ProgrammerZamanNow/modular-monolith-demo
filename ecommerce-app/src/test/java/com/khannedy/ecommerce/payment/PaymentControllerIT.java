package com.khannedy.ecommerce.payment;

import com.khannedy.ecommerce.payment.entity.Payment;
import com.khannedy.ecommerce.payment.model.PaymentResponse;
import com.khannedy.ecommerce.payment.model.PaymentStatusUpdateRequest;
import com.khannedy.ecommerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PaymentRepository paymentRepository;

    private RestTestClient restTestClient;

    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId("order-123");
        payment.setAmount(new BigDecimal("150000.00"));
        payment.setStatus("UNPAID");
        savedPayment = paymentRepository.save(payment);

        this.restTestClient = RestTestClient.bindToApplicationContext(wac).build();
    }

    @Test
    void getPayment_success() {
        restTestClient.get()
                .uri("/api/v1/payments/{id}", savedPayment.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedPayment.getId());
                    assertThat(response.orderId()).isEqualTo("order-123");
                    assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("150000.00"));
                    assertThat(response.status()).isEqualTo("UNPAID");
                });
    }

    @Test
    void getPayment_notFound() {
        restTestClient.get()
                .uri("/api/v1/payments/{id}", "invalid-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePaymentStatus_success() {
        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest("SUCCESS");

        restTestClient.put()
                .uri("/api/v1/payments/{id}/status", savedPayment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(savedPayment.getId());
                    assertThat(response.status()).isEqualTo("SUCCESS");
                });

        // Verify database
        Payment payment = paymentRepository.findById(savedPayment.getId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo("SUCCESS");
    }
}
