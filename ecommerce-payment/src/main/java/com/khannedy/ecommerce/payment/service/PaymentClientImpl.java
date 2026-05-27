package com.khannedy.ecommerce.payment.service;

import com.khannedy.ecommerce.payment.client.PaymentClient;
import com.khannedy.ecommerce.payment.client.PaymentClientRequest;
import com.khannedy.ecommerce.payment.client.PaymentClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentClientImpl implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClientImpl.class);

    private final PaymentService paymentService;

    public PaymentClientImpl(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public PaymentClientResponse createPayment(PaymentClientRequest request) {
        log.info("Creating real payment for order ID: {} with amount: {}", request.orderId(), request.amount());
        
        var paymentResponse = paymentService.createPayment(request.orderId(), request.amount());
        
        return new PaymentClientResponse(paymentResponse.id(), paymentResponse.status());
    }

    @Override
    public void cancelPayment(String paymentId) {
        log.info("Canceling payment ID: {}", paymentId);
        paymentService.updatePaymentStatus(paymentId, new com.khannedy.ecommerce.payment.model.PaymentStatusUpdateRequest("CANCELED"));
    }
}
