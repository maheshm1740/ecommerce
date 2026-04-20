package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddToCartRequest;
import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.UpdateQuantityRequest;
import com.ecommerce.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public List<CartItemResponse> getCart(@AuthenticationPrincipal UserDetails user) {
        return cartService.getCart(user.getUsername());
    }

    @PostMapping("/add")
    public CartItemResponse addToCart(@AuthenticationPrincipal UserDetails user,
                                      @Valid @RequestBody AddToCartRequest req) {
        return cartService.addToCart(user.getUsername(), req.productId(), req.quantity());
    }

    @PutMapping("/{cartItemId}")
    public CartItemResponse updateQuantity(@AuthenticationPrincipal UserDetails user,
                                           @PathVariable Long cartItemId,
                                           @Valid @RequestBody UpdateQuantityRequest req) {
        return cartService.updateQuantity(user.getUsername(), cartItemId, req.quantity());
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UserDetails user,
                                           @PathVariable Long cartItemId) {
        cartService.removeFromCart(user.getUsername(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails user) {
        cartService.clearCart(user.getUsername());
        return ResponseEntity.noContent().build();
    }
}