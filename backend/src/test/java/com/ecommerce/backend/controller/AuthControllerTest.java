package com.ecommerce.backend.controller;

import com.ecommerce.backend.config.TestSecurityConfig;
import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.dto.LoginRequest;
import com.ecommerce.backend.dto.RegisterRequest;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.security.UserDetailsServiceImpl;
import com.ecommerce.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController Integration")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtUtil jwtUtil;                          // needed by JwtFilter constructor
    @MockitoBean UserDetailsServiceImpl userDetailsService; // needed by JwtFilter constructor

    private static final AuthResponse FAKE_RESP =
            new AuthResponse("tok", "user@test.com", "User");

    // ── POST /api/auth/register ───────────────────────────────────────────
    @Test // ✅
    @DisplayName("POST /register → 201 with token when input is valid")
    void register_validInput_returns201() throws Exception {
        when(authService.register(any())).thenReturn(FAKE_RESP);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("User", "user@test.com", "pass123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("tok"))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test // ❌ VALIDATION
    @DisplayName("POST /register → 400 when email is invalid")
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"User\",\"email\":\"not-an-email\",\"password\":\"pass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verify(authService, never()).register(any());
    }

    @Test // ❌ VALIDATION
    @DisplayName("POST /register → 400 when password too short")
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"User\",\"email\":\"u@x.com\",\"password\":\"ab\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test // ❌ DUPLICATE EMAIL
    @DisplayName("POST /register → 409 when email already registered")
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new BusinessException("Email already registered", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("User", "existing@test.com", "pass123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────
    @Test // ✅
    @DisplayName("POST /login → 200 with token when credentials valid")
    void login_validCredentials_returns200() throws Exception {
        when(authService.login(any())).thenReturn(FAKE_RESP);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("user@test.com", "pass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"));
    }

    @Test // ❌
    @DisplayName("POST /login → 401 when credentials are wrong")
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("user@test.com", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
}