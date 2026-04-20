package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal UserDetails user) {
        return orderService.getOrders(user.getUsername());
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(user.getUsername()));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@AuthenticationPrincipal UserDetails user,
                                      @PathVariable Long orderId) {
        return orderService.getOrderById(user.getUsername(), orderId);
    }
}