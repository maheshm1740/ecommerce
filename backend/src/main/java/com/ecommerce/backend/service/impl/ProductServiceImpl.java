package com.ecommerce.backend.service.impl;

import com.ecommerce.backend.dto.CreateProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.mapper.ProductMapper;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)

                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toResponse(product);
    }

    public List<ProductResponse> getByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> search(String query) {
        return productRepository.findByNameContainingIgnoreCase(query).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(CreateProductRequest req) {

        Product saved = productRepository.save(productMapper.toEntity(req));
        return productMapper.toResponse(saved);
    }
}