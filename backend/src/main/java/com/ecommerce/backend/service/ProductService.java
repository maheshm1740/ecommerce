package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CreateProductRequest;
import com.ecommerce.backend.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    List<ProductResponse> getByCategory(String category);

    List<ProductResponse> search(String query);

    ProductResponse createProduct(CreateProductRequest req);
}
