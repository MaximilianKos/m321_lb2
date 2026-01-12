package ch.tbz.products.services;

import ch.tbz.products.dto.*;
import ch.tbz.products.entities.Product;
import ch.tbz.products.exceptions.InsufficientStockException;
import ch.tbz.products.exceptions.ProductNotFoundException;
import ch.tbz.products.exceptions.ProductValidationException;
import ch.tbz.products.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ProductResponse getProductById(UUID productId) {
        log.info("Fetching product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return mapToResponse(product);
    }
    
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        validateProductData(request.getName(), request.getPrice(), request.getStock());
        
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());
        
        return mapToResponse(savedProduct);
    }
    
    @Transactional
    public SuccessResponse updateProduct(UUID productId, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        if (request.getName() != null) {
            if (request.getName().length() < 3) {
                throw new ProductValidationException("Name must be at least 3 characters");
            }
            product.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        
        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) {
                throw new ProductValidationException("Price must be greater than 0");
            }
            product.setPrice(request.getPrice());
        }
        
        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new ProductValidationException("Stock cannot be negative");
            }
            product.setStock(request.getStock());
        }
        
        productRepository.save(product);
        log.info("Product updated successfully: {}", productId);
        
        return SuccessResponse.builder()
                .success(true)
                .message("Product updated successfully")
                .build();
    }
    
    @Transactional
        public void deleteProduct(UUID productId) {
        log.info("Deleting product with ID: {}", productId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        productRepository.delete(product);
        log.info("Product deleted successfully: {}", productId);
        }
    
    public StockResponse checkStock(UUID productId) {
        log.info("Checking stock for product ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        return StockResponse.builder()
                .productId(product.getId())
                .stock(product.getStock())
                .available(product.getStock() > 0)
                .build();
    }
    
    @Transactional
    public SuccessResponse decreaseStock(UUID productId, DecreaseStockRequest request) {
        log.info("Decreasing stock for product ID: {} by quantity: {}", productId, request.getQuantity());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException(
                    product.getName(), 
                    product.getStock(), 
                    request.getQuantity()
            );
        }
        
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);
        
        log.info("Stock decreased successfully. New stock for product {}: {}", productId, product.getStock());
        
        return SuccessResponse.builder()
                .success(true)
                .message("Stock decreased successfully")
                .newStock(product.getStock())
                .build();
    }
    
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    private void validateProductData(String name, Double price, Integer stock) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductValidationException("Product name is required");
        }
        if (name.length() < 3) {
            throw new ProductValidationException("Product name must be at least 3 characters");
        }
        if (price == null || price <= 0) {
            throw new ProductValidationException("Price must be greater than 0");
        }
        if (stock == null || stock < 0) {
            throw new ProductValidationException("Stock cannot be negative");
        }
    }
}
