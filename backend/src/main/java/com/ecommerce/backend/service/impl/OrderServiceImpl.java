package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.mapper.OrderMapper;
import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final CartItemRepository cartItemRepo;
    private final UserRepository userRepo;
    private final OrderMapper orderMapper;

    public List<OrderResponse> getOrders(String email) {
        User user = resolveUser(email);
        return orderRepo.findByUserOrderByCreatedAtDesc(user).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse placeOrder(String email) {
        User user = resolveUser(email);
        List<CartItem> cartItems = cartItemRepo.findByUser(user);

        if (cartItems.isEmpty()) {

            throw new BusinessException("Cart is empty", HttpStatus.BAD_REQUEST);
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        List<Product> products = cartItems.stream()
                .map(CartItem::getProduct)
                .collect(Collectors.toList());

        Order order = Order.builder()
                .user(user)
                .products(products)
                .totalAmount(total)
                .status("CONFIRMED")
                .build();

        Order savedOrder = orderRepo.save(order);
        cartItemRepo.deleteByUser(user);

        return orderMapper.toResponse(savedOrder);
    }

    public OrderResponse getOrderById(String email, Long orderId) {
        User user = resolveUser(email);
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not own this order", HttpStatus.FORBIDDEN);
        }
        return orderMapper.toResponse(order);
    }

    private User resolveUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email));
    }
}