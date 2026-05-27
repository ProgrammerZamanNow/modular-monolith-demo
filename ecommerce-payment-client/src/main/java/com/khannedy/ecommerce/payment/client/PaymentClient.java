package com.khannedy.ecommerce.payment.client;

public interface PaymentClient {
    
    PaymentClientResponse createPayment(PaymentClientRequest request);
    
    void cancelPayment(String paymentId);
    
}
