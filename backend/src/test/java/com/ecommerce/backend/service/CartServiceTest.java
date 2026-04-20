package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.mapper.CartMapper;
import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.service.impl.CartServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService")
class CartServiceTest {

    @Mock CartItemRepository cartItemRepo;
    @Mock ProductRepository  productRepo;
    @Mock UserRepository     userRepo;
    @Mock CartMapper         cartMapper;

    @InjectMocks CartServiceImpl cartService;

    // ── fixtures ──────────────────────────────────────────────────────────
    private static final String EMAIL = "user@test.com";

    private User user() {
        return User.builder().id(1L).email(EMAIL).build();
    }

    private Product product(int stock) {
        return Product.builder().id(10L).name("Widget").price(50.0).stock(stock).build();
    }

    private CartItem cartItem(User u, Product p) {
        return CartItem.builder().id(99L).user(u).product(p).quantity(2).build();
    }

    private CartItemResponse fakeResponse() {
        return new CartItemResponse(99L, null, 2, 100.0);
    }

    // ── getCart ───────────────────────────────────────────────────────────
    @Nested @DisplayName("getCart()")
    class GetCart {

        @Test // ✅
        @DisplayName("should return mapped DTOs for authenticated user")
        void getCart_returnsMappedList() {
            User u = user(); Product p = product(5); CartItem ci = cartItem(u, p);

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(cartItemRepo.findByUser(u)).thenReturn(List.of(ci));
            when(cartMapper.toResponse(ci)).thenReturn(fakeResponse());

            List<CartItemResponse> result = cartService.getCart(EMAIL);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(99L);
        }

        @Test // ❌
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void getCart_unknownUser_throws404() {
            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getCart(EMAIL))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── addToCart ─────────────────────────────────────────────────────────
    @Nested @DisplayName("addToCart()")
    class AddToCart {

        @Test // ✅ NEW ITEM
        @DisplayName("should create new cart item when product not already in cart")
        void addToCart_newItem_createsCartItem() {
            User u = user(); Product p = product(10);
            CartItem newItem = cartItem(u, p);

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(productRepo.findById(10L)).thenReturn(Optional.of(p));
            when(cartItemRepo.findByUserAndProductId(u, 10L)).thenReturn(Optional.empty());
            when(cartItemRepo.save(any(CartItem.class))).thenReturn(newItem);
            when(cartMapper.toResponse(newItem)).thenReturn(fakeResponse());

            CartItemResponse resp = cartService.addToCart(EMAIL, 10L, 2);

            assertThat(resp).isNotNull();
            verify(cartItemRepo).save(any(CartItem.class));
        }

        @Test // ✅ EXISTING ITEM — quantity should accumulate
        @DisplayName("should increment quantity when product already in cart")
        void addToCart_existingItem_incrementsQuantity() {
            User u = user(); Product p = product(10);
            CartItem existing = cartItem(u, p); // quantity = 2

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(productRepo.findById(10L)).thenReturn(Optional.of(p));
            when(cartItemRepo.findByUserAndProductId(u, 10L)).thenReturn(Optional.of(existing));
            when(cartItemRepo.save(existing)).thenReturn(existing);
            when(cartMapper.toResponse(existing)).thenReturn(fakeResponse());

            cartService.addToCart(EMAIL, 10L, 3);

            assertThat(existing.getQuantity()).isEqualTo(5); // 2 + 3
        }

        @Test // ❌ INSUFFICIENT STOCK
        @DisplayName("should throw BusinessException(400) when stock is insufficient")
        void addToCart_insufficientStock_throws400() {
            User u = user(); Product p = product(1); // only 1 in stock

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(productRepo.findById(10L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> cartService.addToCart(EMAIL, 10L, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient stock")
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test // ❌ PRODUCT NOT FOUND
        @DisplayName("should throw ResourceNotFoundException when product does not exist")
        void addToCart_unknownProduct_throws404() {
            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
            when(productRepo.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addToCart(EMAIL, 10L, 1))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product");
        }
    }

    // ── updateQuantity ────────────────────────────────────────────────────
    @Nested @DisplayName("updateQuantity()")
    class UpdateQuantity {

        @Test // ✅
        @DisplayName("should update quantity when user owns the cart item")
        void updateQuantity_success() {
            User u = user(); Product p = product(10);
            CartItem ci = cartItem(u, p);

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(cartItemRepo.findById(99L)).thenReturn(Optional.of(ci));
            when(cartItemRepo.save(ci)).thenReturn(ci);
            when(cartMapper.toResponse(ci)).thenReturn(fakeResponse());

            cartService.updateQuantity(EMAIL, 99L, 7);

            assertThat(ci.getQuantity()).isEqualTo(7);
        }

        @Test // 🔒 SECURITY — another user's cart item
        @DisplayName("should throw BusinessException(FORBIDDEN) when user does not own item")
        void updateQuantity_notOwner_throwsForbidden() {
            User owner  = User.builder().id(1L).email(EMAIL).build();
            User hacker = User.builder().id(2L).email("hacker@test.com").build();
            CartItem ci = cartItem(owner, product(5));

            when(userRepo.findByEmail("hacker@test.com")).thenReturn(Optional.of(hacker));
            when(cartItemRepo.findById(99L)).thenReturn(Optional.of(ci));

            assertThatThrownBy(() -> cartService.updateQuantity("hacker@test.com", 99L, 3))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ── removeFromCart ────────────────────────────────────────────────────
    @Nested @DisplayName("removeFromCart()")
    class RemoveFromCart {

        @Test // ✅
        @DisplayName("should delete item when user owns it")
        void removeFromCart_success() {
            User u = user(); CartItem ci = cartItem(u, product(5));

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(cartItemRepo.findById(99L)).thenReturn(Optional.of(ci));

            cartService.removeFromCart(EMAIL, 99L);

            verify(cartItemRepo).delete(ci);
        }

        @Test // 🔒 SECURITY
        @DisplayName("should throw FORBIDDEN when removing another user's item")
        void removeFromCart_notOwner_throwsForbidden() {
            User owner  = User.builder().id(1L).build();
            User hacker = User.builder().id(2L).email("hacker@test.com").build();
            CartItem ci = cartItem(owner, product(5));

            when(userRepo.findByEmail("hacker@test.com")).thenReturn(Optional.of(hacker));
            when(cartItemRepo.findById(99L)).thenReturn(Optional.of(ci));

            assertThatThrownBy(() -> cartService.removeFromCart("hacker@test.com", 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(cartItemRepo, never()).delete(any());
        }
    }
}