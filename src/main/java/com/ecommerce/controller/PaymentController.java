package com.ecommerce.controller;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.model.Order;
import com.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Stripe payment gateway APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    @Operation(summary = "Create a Stripe PaymentIntent for an order")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long orderId) {
        Map<String, String> response = paymentService.createPaymentIntent(orderId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm payment and update order status")
    public ResponseEntity<Order> confirmPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentRequest request) {
        Order order = paymentService.confirmPayment(
                request.getOrderId(),
                request.getPaymentIntentId(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok(order);
    }
}
