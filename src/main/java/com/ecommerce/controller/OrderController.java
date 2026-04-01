package com.ecommerce.controller;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management and checkout APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout — create order from cart")
    public ResponseEntity<Order> checkout(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody CheckoutRequest request) {
        Order order = orderService.checkout(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    @Operation(summary = "Get all orders for current user")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<Order> getOrderById(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(userDetails.getUsername(), id));
    }
}
