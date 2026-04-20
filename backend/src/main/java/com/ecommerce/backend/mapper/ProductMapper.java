package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.CreateProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getRating(),
                product.getReviewCount()
        );
    }

    public Product toEntity(CreateProductRequest req) {
        return Product.builder()
                .name(req.name())
                .description(req.description())
                .category(req.category())
                .imageUrl(req.imageUrl())
                .price(req.price())
                .stock(req.stock())
                .build();
    }
}