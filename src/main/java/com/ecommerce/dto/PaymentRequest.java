package com.ecommerce.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private String paymentIntentId;
}
