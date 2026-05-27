package com.khannedy.ecommerce.payment.client.impl;

import com.khannedy.ecommerce.payment.client.PaymentClient;
import com.khannedy.ecommerce.payment.client.PaymentClientRequest;
import com.khannedy.ecommerce.payment.client.PaymentClientResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PaymentClientRestImpl implements PaymentClient {

    private final RestClient restClient;

    public PaymentClientRestImpl() {
        this.restClient = RestClient.create("http://payment-microservice");
    }

    @Override
    public PaymentClientResponse createPayment(PaymentClientRequest request) {
        // Simulasi HTTP POST request ke microservice payment
        return restClient.post()
                .uri("/api/v1/payments")
                .body(request)
                .retrieve()
                .body(PaymentClientResponse.class);
    }

    @Override
    public void cancelPayment(String paymentId) {
        // Simulasi HTTP POST request untuk cancel payment
        restClient.post()
                .uri("/api/v1/payments/{id}/cancel", paymentId)
                .retrieve()
                .toBodilessEntity();
    }
}
