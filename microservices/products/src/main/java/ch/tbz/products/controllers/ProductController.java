package ch.tbz.products.controllers;

import ch.tbz.products.dto.*;
import ch.tbz.products.services.AuthService;
import ch.tbz.products.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    private final AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("GET /api/products - Fetching all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") UUID productId) {
        log.info("GET /api/products/{} - Fetching product details", productId);
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/products - Creating new product: {}", request.getName());
        
        String token = authService.extractToken(authHeader);
        authService.validateAdminToken(token);
        
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PutMapping("/{productId}")
    public ResponseEntity<SuccessResponse> updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /api/products/{} - Updating product", productId);
        
        String token = authService.extractToken(authHeader);
        authService.validateAdminToken(token);
        
        SuccessResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("productId") UUID productId) {
        log.info("DELETE /api/products/{} - Deleting product", productId);
        
        String token = authService.extractToken(authHeader);
        authService.validateAdminToken(token);
        
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{productId}/stock")
    public ResponseEntity<StockResponse> checkStock(@PathVariable("productId") UUID productId) {
        log.info("GET /api/products/{}/stock - Checking stock", productId);
        StockResponse response = productService.checkStock(productId);
        return ResponseEntity.ok(response);
    }

    //internal use
    @PostMapping("/{productId}/decrease")
    public ResponseEntity<SuccessResponse> decreaseStock(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody DecreaseStockRequest request) {
        log.info("POST /api/products/{}/decrease - Decreasing stock by {}", productId, request.getQuantity());
        SuccessResponse response = productService.decreaseStock(productId, request);
        return ResponseEntity.ok(response);
    }
}