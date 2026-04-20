package com.ecommerce.backend.mapper;

import com.ecommerce.backend.dto.CreateProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.model.Product;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test // ✅
    void toResponse_mapsAllFields() {
        Product p = Product.builder()
                .id(1L).name("Laptop").description("Fast").category("TECH")
                .imageUrl("img.png").price(999.0).stock(5).rating(4.5).reviewCount(12)
                .build();

        ProductResponse r = mapper.toResponse(p);

        assertThat(r.id()).isEqualTo(1L);
        assertThat(r.name()).isEqualTo("Laptop");
        assertThat(r.price()).isEqualTo(999.0);
        assertThat(r.rating()).isEqualTo(4.5);
        assertThat(r.reviewCount()).isEqualTo(12);
    }

    @Test // ✅
    void toEntity_doesNotSetIdOrRating() {
        CreateProductRequest req = new CreateProductRequest(
                "Phone", "Smart", "MOBILES", "phone.jpg", 499.0, 20);

        Product entity = mapper.toEntity(req);

        assertThat(entity.getId()).isNull();         // client must not control PK
        assertThat(entity.getRating()).isNull();     // rating set by reviews, not creation
        assertThat(entity.getName()).isEqualTo("Phone");
        assertThat(entity.getPrice()).isEqualTo(499.0);
    }
}