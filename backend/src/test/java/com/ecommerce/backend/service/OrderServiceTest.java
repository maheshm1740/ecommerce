package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.mapper.OrderMapper;
import com.ecommerce.backend.model.*;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock OrderRepository    orderRepo;
    @Mock CartItemRepository cartItemRepo;
    @Mock UserRepository     userRepo;
    @Mock OrderMapper        orderMapper;

    @InjectMocks OrderServiceImpl orderService;

    private static final String EMAIL = "user@test.com";

    private User user() { return User.builder().id(1L).email(EMAIL).build(); }

    private Product product() {
        return Product.builder().id(10L).price(100.0).build();
    }

    private CartItem cartItem(User u, Product p) {
        return CartItem.builder().user(u).product(p).quantity(2).build();
    }

    private Order placedOrder(User u, Product p) {
        return Order.builder()
                .id(50L).user(u).products(List.of(p))
                .totalAmount(200.0).status("CONFIRMED")
                .createdAt(LocalDateTime.now()).build();
    }

    private OrderResponse fakeResponse(Long id) {
        return new OrderResponse(id, List.of(), 200.0, "CONFIRMED", LocalDateTime.now());
    }

    // ── placeOrder ────────────────────────────────────────────────────────
    @Nested @DisplayName("placeOrder()")
    class PlaceOrder {

        @Test // ✅
        @DisplayName("should create order and clear cart when cart has items")
        void placeOrder_success_clearsCart() {
            User u = user(); Product p = product();
            CartItem ci = cartItem(u, p);
            Order order = placedOrder(u, p);

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(cartItemRepo.findByUser(u)).thenReturn(List.of(ci));
            when(orderRepo.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(fakeResponse(50L));

            OrderResponse resp = orderService.placeOrder(EMAIL);

            assertThat(resp.id()).isEqualTo(50L);
            assertThat(resp.totalAmount()).isEqualTo(200.0);

            verify(orderRepo).save(any(Order.class));
            verify(cartItemRepo).deleteByUser(u);   // cart MUST be cleared
        }

        @Test // ✅ TOTAL CALCULATION
        @DisplayName("should calculate total as sum of price * quantity for all items")
        void placeOrder_totalCalculation_isCorrect() {
            User u = user();
            Product p1 = Product.builder().id(1L).price(10.0).build();
            Product p2 = Product.builder().id(2L).price(25.0).build();
            CartItem ci1 = CartItem.builder().user(u).product(p1).quantity(3).build(); // 30.0
            CartItem ci2 = CartItem.builder().user(u).product(p2).quantity(2).build(); // 50.0

            Order order = Order.builder().id(1L).user(u)
                    .products(List.of(p1, p2)).totalAmount(80.0)
                    .status("CONFIRMED").createdAt(LocalDateTime.now()).build();

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(cartItemRepo.findByUser(u)).thenReturn(List.of(ci1, ci2));
            when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
                Order saved = inv.getArgument(0);
                assertThat(saved.getTotalAmount()).isEqualTo(80.0); // verify before returning
                return order;
            });
            when(orderMapper.toResponse(any())).thenReturn(fakeResponse(1L));

            orderService.placeOrder(EMAIL);
        }

        @Test // ❌ EMPTY CART
        @DisplayName("should throw BusinessException(400) when cart is empty")
        void placeOrder_emptyCart_throws400() {
            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
            when(cartItemRepo.findByUser(any())).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> orderService.placeOrder(EMAIL))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("empty")
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            verify(orderRepo, never()).save(any()); // no order should be saved
        }
    }

    // ── getOrderById ──────────────────────────────────────────────────────
    @Nested @DisplayName("getOrderById()")
    class GetOrderById {

        @Test // ✅
        @DisplayName("should return order when user owns it")
        void getOrderById_success() {
            User u = user(); Order o = placedOrder(u, product());

            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(u));
            when(orderRepo.findById(50L)).thenReturn(Optional.of(o));
            when(orderMapper.toResponse(o)).thenReturn(fakeResponse(50L));

            OrderResponse resp = orderService.getOrderById(EMAIL, 50L);

            assertThat(resp.id()).isEqualTo(50L);
        }

        @Test // ❌ NOT FOUND
        @DisplayName("should throw ResourceNotFoundException when order does not exist")
        void getOrderById_notFound_throws404() {
            when(userRepo.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
            when(orderRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(EMAIL, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test // 🔒 SECURITY
        @DisplayName("should throw FORBIDDEN when user does not own the order")
        void getOrderById_notOwner_throwsForbidden() {
            User owner  = User.builder().id(1L).build();
            User hacker = User.builder().id(2L).email("hacker@test.com").build();
            Order o     = placedOrder(owner, product());

            when(userRepo.findByEmail("hacker@test.com")).thenReturn(Optional.of(hacker));
            when(orderRepo.findById(50L)).thenReturn(Optional.of(o));

            assertThatThrownBy(() -> orderService.getOrderById("hacker@test.com", 50L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
}