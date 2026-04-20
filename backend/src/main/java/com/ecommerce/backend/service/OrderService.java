package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getOrders(String email);

    OrderResponse placeOrder(String email);

    OrderResponse getOrderById(String email, Long orderId);
}
