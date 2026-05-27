package com.khannedy.ecommerce.payment.controller;

import com.khannedy.ecommerce.payment.model.PaymentResponse;
import com.khannedy.ecommerce.payment.model.PaymentStatusUpdateRequest;
import com.khannedy.ecommerce.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable("id") String id) {
        return paymentService.getPayment(id);
    }

    @PutMapping("/{id}/status")
    public PaymentResponse updatePaymentStatus(@PathVariable("id") String id, @RequestBody PaymentStatusUpdateRequest request) {
        return paymentService.updatePaymentStatus(id, request);
    }
}
