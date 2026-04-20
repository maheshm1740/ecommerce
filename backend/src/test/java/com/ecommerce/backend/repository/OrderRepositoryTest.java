package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.*;
import com.ecommerce.backend.model.Order;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("OrderRepository")
class OrderRepositoryTest {

    @Autowired OrderRepository   orderRepo;
    @Autowired UserRepository    userRepo;
    @Autowired ProductRepository productRepo;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepo.save(
                User.builder().email("order@test.com").password("h").name("Order User").build());
    }

    @Test // ✅ ORDERED BY DATE DESC
    @DisplayName("findByUserOrderByCreatedAtDesc() should return newest first")
    void findByUser_sortedDescByDate() throws InterruptedException {
        Order older = orderRepo.save(Order.builder()
                .user(user).totalAmount(10.0).status("CONFIRMED")
                .createdAt(LocalDateTime.now().minusDays(2)).products(List.of()).build());

        Order newer = orderRepo.save(Order.builder()
                .user(user).totalAmount(20.0).status("CONFIRMED")
                .createdAt(LocalDateTime.now()).products(List.of()).build());

        List<Order> orders = orderRepo.findByUserOrderByCreatedAtDesc(user);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isEqualTo(newer.getId()); // newest first
        assertThat(orders.get(1).getId()).isEqualTo(older.getId());
    }

    @Test // ✅
    @DisplayName("findByUser() should not return orders from other users")
    void findByUser_isolatesPerUser() {
        User otherUser = userRepo.save(
                User.builder().email("other@test.com").password("h").name("Other").build());
        orderRepo.save(Order.builder()
                .user(otherUser).totalAmount(5.0).status("CONFIRMED")
                .createdAt(LocalDateTime.now()).products(List.of()).build());

        List<Order> orders = orderRepo.findByUserOrderByCreatedAtDesc(user);

        assertThat(orders).isEmpty(); // user's orders should be isolated
    }
}