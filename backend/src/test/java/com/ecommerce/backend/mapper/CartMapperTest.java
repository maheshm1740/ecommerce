package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartMapperTest {

    @Mock ProductMapper productMapper;
    @InjectMocks CartMapper cartMapper;

    @Test // ✅ subtotal = price * quantity
    void toResponse_computesSubtotalCorrectly() {
        Product product = Product.builder().id(1L).price(25.0).build();
        User user = User.builder().id(1L).build();
        CartItem item = CartItem.builder().id(5L).user(user).product(product).quantity(4).build();

        ProductResponse pr = new ProductResponse(1L, "Thing", null, null, null, 25.0, 10, 0.0, 0);
        when(productMapper.toResponse(product)).thenReturn(pr);

        CartItemResponse resp = cartMapper.toResponse(item);

        assertThat(resp.id()).isEqualTo(5L);
        assertThat(resp.quantity()).isEqualTo(4);
        assertThat(resp.subtotal()).isEqualTo(100.0);  // 25 * 4
    }
}