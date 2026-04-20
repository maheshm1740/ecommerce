package com.ecommerce.backend.dto;

public record AuthResponse(String token, String email, String name, String role) {}