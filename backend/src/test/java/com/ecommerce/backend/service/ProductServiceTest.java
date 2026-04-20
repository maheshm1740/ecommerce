package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CreateProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.mapper.ProductMapper;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock ProductRepository productRepo;
    @Mock ProductMapper     productMapper;

    @InjectMocks ProductServiceImpl productService;

    private Product product(Long id, String cat) {
        return Product.builder().id(id).name("Item " + id)
                .category(cat).price(99.0).stock(10).build();
    }

    private ProductResponse response(Long id) {
        return new ProductResponse(id, "Item " + id, null, "ELECTRONICS",
                null, 99.0, 10, 0.0, 0);
    }

    @Test // ✅
    @DisplayName("getAllProducts() should return all mapped products")
    void getAllProducts_returnsMappedList() {
        Product p1 = product(1L, "ELECTRONICS");
        Product p2 = product(2L, "BOOKS");

        when(productRepo.findAll()).thenReturn(List.of(p1, p2));
        when(productMapper.toResponse(p1)).thenReturn(response(1L));
        when(productMapper.toResponse(p2)).thenReturn(response(2L));

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
    }

    @Test // ✅
    @DisplayName("getProductById() should return mapped DTO for valid id")
    void getProductById_found_returnsMapped() {
        Product p = product(1L, "BOOKS");
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(productMapper.toResponse(p)).thenReturn(response(1L));

        ProductResponse resp = productService.getProductById(1L);

        assertThat(resp.id()).isEqualTo(1L);
    }

    @Test // ❌
    @DisplayName("getProductById() should throw ResourceNotFoundException for unknown id")
    void getProductById_notFound_throws404() {
        when(productRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test // ✅
    @DisplayName("createProduct() should not allow client to set id or rating")
    void createProduct_mapsFromRequestNotEntity() {
        CreateProductRequest req = new CreateProductRequest(
                "Widget", "Desc", "GADGETS", null, 29.99, 100);
        Product mapped  = product(null, "GADGETS");
        Product saved   = product(5L,   "GADGETS");

        when(productMapper.toEntity(req)).thenReturn(mapped);
        when(productRepo.save(mapped)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(response(5L));

        ProductResponse resp = productService.createProduct(req);

        assertThat(resp.id()).isEqualTo(5L);
        verify(productMapper).toEntity(req);  // must go through mapper, not direct entity
    }

    @Test // ✅
    @DisplayName("search() should delegate to repository case-insensitive search")
    void search_delegatesToRepository() {
        Product p = product(1L, "BOOKS");
        when(productRepo.findByNameContainingIgnoreCase("widget")).thenReturn(List.of(p));
        when(productMapper.toResponse(p)).thenReturn(response(1L));

        List<ProductResponse> result = productService.search("widget");

        assertThat(result).hasSize(1);
    }
}