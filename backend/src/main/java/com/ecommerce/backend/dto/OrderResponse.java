package com.ecommerce.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        List<ProductResponse> products,
        Double totalAmount,
        String status,
        LocalDateTime createdAt
) {}