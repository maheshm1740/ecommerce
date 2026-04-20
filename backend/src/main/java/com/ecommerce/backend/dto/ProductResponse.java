package com.ecommerce.backend.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String category,
        String imageUrl,
        Double price,
        Integer stock,
        Double rating,
        Integer reviewCount
) {}