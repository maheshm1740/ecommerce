package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemResponse;

import java.util.List;

public interface CartService {

    List<CartItemResponse> getCart(String email);

    CartItemResponse addToCart(String email, Long productId, Integer quantity);

    CartItemResponse updateQuantity(String email, Long cartItemId, Integer quantity);

    void removeFromCart(String email, Long cartItemId);

    void clearCart(String email);
}
