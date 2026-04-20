package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final ProductMapper productMapper;

    public CartItemResponse toResponse(CartItem item) {
        double subtotal = item.getProduct().getPrice() * item.getQuantity();
        return new CartItemResponse(
                item.getId(),
                productMapper.toResponse(item.getProduct()),
                item.getQuantity(),
                subtotal
        );
    }
}