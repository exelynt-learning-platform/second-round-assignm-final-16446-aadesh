package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", "sk_test_YOUR_STRIPE_SECRET_KEY_HERE");

        user = new User();
        user.setId(1L);
        user.setEmail("jane@shop.com");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalPrice(new BigDecimal("599.98"));
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
    }

    @Test
    void createPaymentIntent_shouldReturnMockIntentInDemoMode() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Map<String, String> result = paymentService.createPaymentIntent(1L, "jane@shop.com");

        assertNotNull(result);
        assertTrue(result.get("paymentIntentId").startsWith("pi_mock_"));
        assertEquals("1", result.get("orderId"));
        assertNotNull(result.get("clientSecret"));
    }

    @Test
    void createPaymentIntent_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createPaymentIntent(99L, "jane@shop.com"));
    }

    @Test
    void createPaymentIntent_shouldThrowWhenOrderBelongsToDifferentUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createPaymentIntent(1L, "other@shop.com"));

        assertEquals("You can only pay for your own order", ex.getMessage());
    }

    @Test
    void createPaymentIntent_shouldThrowWhenOrderAlreadyPaid() {
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createPaymentIntent(1L, "jane@shop.com"));

        assertEquals("This order is already paid", ex.getMessage());
    }

    @Test
    void confirmPayment_shouldMarkOrderAsPaidInMockMode() {
        Order paidOrder = new Order();
        paidOrder.setId(1L);
        paidOrder.setUser(user);
        paidOrder.setPaymentStatus(Order.PaymentStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderService.updatePaymentStatus(1L, Order.PaymentStatus.PAID, "pi_mock_1_123"))
                .thenReturn(paidOrder);

        Order result = paymentService.confirmPayment(1L, "pi_mock_1_123", "jane@shop.com");

        assertEquals(Order.PaymentStatus.PAID, result.getPaymentStatus());
        verify(orderService).updatePaymentStatus(1L, Order.PaymentStatus.PAID, "pi_mock_1_123");
    }
}
