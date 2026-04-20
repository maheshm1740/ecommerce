package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CreateProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> all() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse one(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/category/{cat}")
    public List<ProductResponse> byCategory(@PathVariable String cat) {
        return productService.getByCategory(cat);
    }

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String q) {
        return productService.search(q);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(req));
    }
}