package com.khannedy.ecommerce.order.service;

import com.khannedy.ecommerce.customer.client.CustomerClient;
import com.khannedy.ecommerce.customer.client.CustomerClientResponse;
import com.khannedy.ecommerce.notification.client.NotificationClient;
import com.khannedy.ecommerce.notification.client.NotificationClientRequest;
import com.khannedy.ecommerce.order.entity.Order;
import com.khannedy.ecommerce.order.entity.OrderItem;
import com.khannedy.ecommerce.order.model.OrderItemRequest;
import com.khannedy.ecommerce.order.model.OrderItemResponse;
import com.khannedy.ecommerce.order.model.OrderRequest;
import com.khannedy.ecommerce.order.model.OrderResponse;
import com.khannedy.ecommerce.order.repository.OrderRepository;
import com.khannedy.ecommerce.payment.client.PaymentClient;
import com.khannedy.ecommerce.payment.client.PaymentClientRequest;
import com.khannedy.ecommerce.payment.client.PaymentClientResponse;
import com.khannedy.ecommerce.product.model.ProductResponse;
import com.khannedy.ecommerce.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductService productClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;
    private final CustomerClient customerClient;

    public OrderService(OrderRepository orderRepository,
                        ProductService productClient,
                        PaymentClient paymentClient,
                        NotificationClient notificationClient,
                        CustomerClient customerClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
        this.notificationClient = notificationClient;
        this.customerClient = customerClient;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        CustomerClientResponse customer = customerClient.getCustomer(request.customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + request.customerId()));

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCustomerId(customer.id());
        order.setStatus("PENDING");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            // 1. Fetch Product Data from ProductClient
            ProductResponse product = productClient.getById(itemRequest.productId());

            if (product.stock() < itemRequest.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.name());
            }

            productClient.reduceStock(product.id(), itemRequest.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setProductId(product.id());
            orderItem.setPrice(product.price());
            orderItem.setQuantity(itemRequest.quantity());
            
            order.addItem(orderItem);

            BigDecimal itemTotal = product.price().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        // 2. Save Order to Database
        Order savedOrder = orderRepository.save(order);

        // 3. Create Payment via PaymentClient
        try {
            PaymentClientRequest paymentRequest = new PaymentClientRequest(savedOrder.getId(), savedOrder.getTotalAmount());
            PaymentClientResponse paymentResponse = paymentClient.createPayment(paymentRequest);
            
            savedOrder.setPaymentId(paymentResponse.paymentId());
            savedOrder.setStatus(paymentResponse.status());
            savedOrder = orderRepository.save(savedOrder);
        } catch (Exception e) {
            log.error("Failed to create payment for order {}: {}", savedOrder.getId(), e.getMessage());
            // Depending on requirements, we could fail the transaction or keep it PENDING
            // We'll keep it PENDING for now
        }

        // 4. Send Notification
        try {
            String message = String.format("Order %s has been created for %s with total %s", 
                    savedOrder.getId(), customer.name(), savedOrder.getTotalAmount());
            NotificationClientRequest notifRequest = new NotificationClientRequest(customer.email(), message);
            notificationClient.sendNotification(notifRequest);
        } catch (Exception e) {
            log.error("Failed to send notification for order {}: {}", savedOrder.getId(), e.getMessage());
        }

        return toResponse(savedOrder, customer.name());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        
        CustomerClientResponse customer = customerClient.getCustomer(order.getCustomerId())
                .orElse(new CustomerClientResponse(order.getCustomerId(), "Unknown", "", ""));
                
        return toResponse(order, customer.name());
    }

    @Transactional
    public OrderResponse cancelOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if ("CANCELED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is already canceled");
        }

        order.setStatus("CANCELED");

        for (OrderItem item : order.getItems()) {
            try {
                productClient.restoreStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to restore stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }

        if (order.getPaymentId() != null) {
            try {
                paymentClient.cancelPayment(order.getPaymentId());
            } catch (Exception e) {
                log.error("Failed to cancel payment {}: {}", order.getPaymentId(), e.getMessage());
            }
        }

        order = orderRepository.save(order);
        
        CustomerClientResponse customer = customerClient.getCustomer(order.getCustomerId())
                .orElse(new CustomerClientResponse(order.getCustomerId(), "Unknown", "", ""));

        return toResponse(order, customer.name());
    }

    private OrderResponse toResponse(Order order, String customerName) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                customerName,
                order.getTotalAmount(),
                order.getPaymentId(),
                order.getStatus(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}
