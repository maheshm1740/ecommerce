package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProductId(User user, Long productId);

    void deleteByUser(User user);
}