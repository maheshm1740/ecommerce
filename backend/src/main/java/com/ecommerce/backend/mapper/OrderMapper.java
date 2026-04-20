package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ProductMapper productMapper;

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProducts().stream()
                        .map(productMapper::toResponse)
                        .collect(Collectors.toList()),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}