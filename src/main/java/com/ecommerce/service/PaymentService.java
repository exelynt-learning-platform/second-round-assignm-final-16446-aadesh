package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private boolean isMockMode() {
        return stripeSecretKey == null
                || stripeSecretKey.isBlank()
                || stripeSecretKey.contains("YOUR_STRIPE_SECRET_KEY");
    }

    public Map<String, String> createPaymentIntent(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You can only pay for your own order");
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new RuntimeException("This order is already paid");
        }

        if (isMockMode()) {
            String mockIntentId = "pi_mock_" + orderId + "_" + System.currentTimeMillis();
            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", mockIntentId + "_secret_mock");
            response.put("paymentIntentId", mockIntentId);
            response.put("orderId", orderId.toString());
            response.put("note", "DEMO MODE - Add real Stripe key in application.properties for live payments");
            return response;
        }

        try {
            long amountInCents = order.getTotalPrice()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .putMetadata("orderId", orderId.toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("paymentIntentId", intent.getId());
            response.put("orderId", orderId.toString());
            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    public Order confirmPayment(Long orderId, String paymentIntentId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You can only confirm payment for your own order");
        }

        if (isMockMode() || paymentIntentId.startsWith("pi_mock_")) {
            return orderService.updatePaymentStatus(orderId, Order.PaymentStatus.PAID, paymentIntentId);
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(intent.getStatus())) {
                return orderService.updatePaymentStatus(orderId, Order.PaymentStatus.PAID, paymentIntentId);
            } else {
                return orderService.updatePaymentStatus(orderId, Order.PaymentStatus.FAILED, paymentIntentId);
            }

        } catch (StripeException e) {
            orderService.updatePaymentStatus(orderId, Order.PaymentStatus.FAILED, paymentIntentId);
            throw new RuntimeException("Payment confirmation failed: " + e.getMessage());
        }
    }
}
