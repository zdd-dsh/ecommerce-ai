package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartCheckoutRequest;
import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.OrderDetailResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {

    List<CartItemResponse> getCart(Long userId);

    void addItem(Long userId, CartItemRequest request);

    void updateItem(Long userId, Long itemId, CartItemRequest request);

    void removeItem(Long userId, Long itemId);

    void clear(Long userId);

    OrderDetailResponse checkout(Long userId, CartCheckoutRequest request);

}
