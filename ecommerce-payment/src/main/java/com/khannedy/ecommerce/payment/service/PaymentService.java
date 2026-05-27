package com.khannedy.ecommerce.payment.service;

import com.khannedy.ecommerce.payment.entity.Payment;
import com.khannedy.ecommerce.payment.model.PaymentResponse;
import com.khannedy.ecommerce.payment.model.PaymentStatusUpdateRequest;
import com.khannedy.ecommerce.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse createPayment(String orderId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("PAID"); // Setting default success for now, as order module expects this in its workflow
        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String id) {
        Payment payment = findByIdOrThrow(id);
        return toResponse(payment);
    }

    public PaymentResponse updatePaymentStatus(String id, PaymentStatusUpdateRequest request) {
        Payment payment = findByIdOrThrow(id);
        payment.setStatus(request.status());
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    private Payment findByIdOrThrow(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
