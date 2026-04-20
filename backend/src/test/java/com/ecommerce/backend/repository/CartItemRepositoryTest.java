package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("CartItemRepository")
class CartItemRepositoryTest {

    @Autowired CartItemRepository cartItemRepo;
    @Autowired UserRepository     userRepo;
    @Autowired ProductRepository  productRepo;

    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedUser = userRepo.save(
                User.builder().email("cart@test.com").password("h").name("Cart User").build());
        savedProduct = productRepo.save(
                Product.builder().name("Widget").price(10.0).stock(100).build());
    }

    @Test // ✅
    @DisplayName("findByUser() should return all items for given user")
    void findByUser_returnsItems() {
        cartItemRepo.save(CartItem.builder()
                .user(savedUser).product(savedProduct).quantity(2).build());

        List<CartItem> items = cartItemRepo.findByUser(savedUser);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
    }

    @Test // ✅
    @DisplayName("findByUserAndProductId() should return existing cart entry")
    void findByUserAndProductId_returnsItem() {
        cartItemRepo.save(CartItem.builder()
                .user(savedUser).product(savedProduct).quantity(1).build());

        Optional<CartItem> found =
                cartItemRepo.findByUserAndProductId(savedUser, savedProduct.getId());

        assertThat(found).isPresent();
    }

    @Test // ✅
    @DisplayName("deleteByUser() should remove all items for user")
    void deleteByUser_clearsCart() {
        cartItemRepo.save(CartItem.builder()
                .user(savedUser).product(savedProduct).quantity(3).build());

        cartItemRepo.deleteByUser(savedUser);

        assertThat(cartItemRepo.findByUser(savedUser)).isEmpty();
    }
}