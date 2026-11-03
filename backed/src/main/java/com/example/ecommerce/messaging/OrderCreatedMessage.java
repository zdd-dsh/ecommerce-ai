package com.example.ecommerce.messaging;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreatedMessage {
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private List<Long> productIds;

}
