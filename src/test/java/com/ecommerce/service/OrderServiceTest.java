package com.ecommerce.service;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@test.com");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setStockQuantity(5);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));
    }

    @Test
    void checkout_shouldCreateOrderFromCart() {
        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddress("123 Main St, New York");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setTotalPrice(new BigDecimal("1999.98"));
        savedOrder.setPaymentStatus(Order.PaymentStatus.PENDING);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Order result = orderService.checkout("john@test.com", request);

        assertNotNull(result);
        assertEquals(Order.PaymentStatus.PENDING, result.getPaymentStatus());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void checkout_shouldThrowWhenCartIsEmpty() {
        cart.setItems(new ArrayList<>());

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout("john@test.com", new CheckoutRequest()));

        assertTrue(ex.getMessage().contains("Cart is empty"));
    }

    @Test
    void getOrderById_shouldThrowWhenOrderBelongsToDifferentUser() {
        User otherUser = new User();
        otherUser.setEmail("other@test.com");

        Order order = new Order();
        order.setId(1L);
        order.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(Exception.class,
                () -> orderService.getOrderById("john@test.com", 1L));
    }

    @Test
    void getUserOrders_shouldReturnOrdersForUser() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(List.of());

        List<Order> result = orderService.getUserOrders("john@test.com");

        assertNotNull(result);
        verify(orderRepository).findByUser(user);
    }
}
