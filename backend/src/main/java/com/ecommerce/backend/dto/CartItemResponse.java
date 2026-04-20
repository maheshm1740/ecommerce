package com.ecommerce.backend.dto;

public record CartItemResponse(
        Long id,
        ProductResponse product,
        Integer quantity,
        Double subtotal
) {}