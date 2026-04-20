package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.mapper.CartMapper;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CartMapper cartMapper;

    public List<CartItemResponse> getCart(String email) {
        User user = resolveUser(email);
        return cartItemRepo.findByUser(user).stream()
                .map(cartMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CartItemResponse addToCart(String email, Long productId, Integer quantity) {
        User user = resolveUser(email);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (product.getStock() < quantity) {
            throw new BusinessException(
                    "Insufficient stock. Available: " + product.getStock(), HttpStatus.BAD_REQUEST);
        }

        CartItem saved = cartItemRepo.findByUserAndProductId(user, productId)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return cartItemRepo.save(existing);
                })
                .orElseGet(() -> cartItemRepo.save(
                        CartItem.builder()
                                .user(user)
                                .product(product)
                                .quantity(quantity)
                                .build()
                ));
        return cartMapper.toResponse(saved);
    }

    public CartItemResponse updateQuantity(String email, Long cartItemId, Integer quantity) {
        User user = resolveUser(email);
        CartItem item = cartItemRepo.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getUser().getId().equals(user.getId())) {

            throw new BusinessException("You do not own this cart item", HttpStatus.FORBIDDEN);
        }
        item.setQuantity(quantity);
        return cartMapper.toResponse(cartItemRepo.save(item));
    }

    public void removeFromCart(String email, Long cartItemId) {
        User user = resolveUser(email);
        CartItem item = cartItemRepo.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not own this cart item", HttpStatus.FORBIDDEN);
        }
        cartItemRepo.delete(item);
    }

    @Transactional
    public void clearCart(String email) {
        User user = resolveUser(email);
        cartItemRepo.deleteByUser(user);
    }

    private User resolveUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email));
    }
}