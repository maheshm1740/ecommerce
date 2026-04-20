package com.ecommerce.backend.controller;

import com.ecommerce.backend.config.TestSecurityConfig;
import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.security.UserDetailsServiceImpl;
import com.ecommerce.backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(TestSecurityConfig.class)
@DisplayName("CartController Integration")
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CartService cartService;
    @MockitoBean JwtUtil jwtUtil;                          // needed by JwtFilter constructor
    @MockitoBean UserDetailsServiceImpl userDetailsService; // needed by JwtFilter constructor

    private static final ProductResponse PROD =
            new ProductResponse(1L, "Widget", null, "TECH", null, 50.0, 10, 4.0, 5);
    private static final CartItemResponse ITEM =
            new CartItemResponse(1L, PROD, 2, 100.0);

    @Test // ✅
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /api/cart → 200 with cart items")
    void getCart_authenticated_returns200() throws Exception {
        when(cartService.getCart("user@test.com")).thenReturn(List.of(ITEM));

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].subtotal").value(100.0))
                .andExpect(jsonPath("$[0].product.name").value("Widget"));
    }

    @Test // 🔒 SECURITY
    @DisplayName("GET /api/cart → 401 when not authenticated")
    void getCart_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test // ✅
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /api/cart/add → 200 with valid request")
    void addToCart_validRequest_returns200() throws Exception {
        when(cartService.addToCart(eq("user@test.com"), eq(1L), eq(2))).thenReturn(ITEM);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(100.0));
    }

    @Test // ❌ VALIDATION
    @WithMockUser
    @DisplayName("POST /api/cart/add → 400 when quantity is 0")
    void addToCart_zeroQuantity_returns400() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test // ❌ VALIDATION
    @WithMockUser
    @DisplayName("POST /api/cart/add → 400 when productId is missing")
    void addToCart_missingProductId_returns400() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.productId").exists());
    }

    @Test // ✅
    @WithMockUser(username = "user@test.com")
    @DisplayName("DELETE /api/cart/{id} → 204 No Content")
    void removeItem_returns204() throws Exception {
        doNothing().when(cartService).removeFromCart(anyString(), eq(1L));

        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isNoContent());
    }
}