package com.ecommerce.backend.controller;

import com.ecommerce.backend.config.TestSecurityConfig;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.security.UserDetailsServiceImpl;
import com.ecommerce.backend.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(TestSecurityConfig.class)
@DisplayName("OrderController Integration")
class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean OrderService orderService;
    @MockitoBean JwtUtil jwtUtil;                          // needed by JwtFilter constructor
    @MockitoBean UserDetailsServiceImpl userDetailsService; // needed by JwtFilter constructor

    private static final OrderResponse FAKE_ORDER =
            new OrderResponse(1L, List.of(), 200.0, "CONFIRMED", LocalDateTime.now());

    @Test // ✅
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /api/orders/place → 201 with order when cart has items")
    void placeOrder_success_returns201() throws Exception {
        when(orderService.placeOrder("user@test.com")).thenReturn(FAKE_ORDER);

        mockMvc.perform(post("/api/orders/place"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(200.0));
    }

    @Test // ❌ EMPTY CART
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /api/orders/place → 400 when cart is empty")
    void placeOrder_emptyCart_returns400() throws Exception {
        when(orderService.placeOrder(anyString()))
                .thenThrow(new BusinessException("Cart is empty", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/orders/place"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cart is empty"));
    }

    @Test // ✅
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /api/orders → 200 with list of orders")
    void getOrders_authenticated_returns200() throws Exception {
        when(orderService.getOrders("user@test.com")).thenReturn(List.of(FAKE_ORDER));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test // 🔒 SECURITY
    @WithMockUser(username = "hacker@test.com")
    @DisplayName("GET /api/orders/{id} → 403 when user does not own order")
    void getOrderById_forbidden_returns403() throws Exception {
        when(orderService.getOrderById(eq("hacker@test.com"), eq(1L)))
                .thenThrow(new BusinessException("You do not own this order", HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isForbidden());
    }
}