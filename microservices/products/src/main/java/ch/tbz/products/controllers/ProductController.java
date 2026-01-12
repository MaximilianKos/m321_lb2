package ch.tbz.products.controllers;

import ch.tbz.products.dto.*;
import ch.tbz.products.services.AuthService;
import ch.tbz.products.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Products", description = "Produktkatalog und Lagerbestand")
public class ProductController {
    
    private final ProductService productService;
    private final AuthService authService;
    
    @Operation(summary = "Alle Produkte auflisten")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste der Produkte",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("GET /api/products - Fetching all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
        @Operation(summary = "Produktdetails abrufen")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produkt gefunden",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") UUID productId) {
        log.info("GET /api/products/{} - Fetching product details", productId);
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    
        @Operation(summary = "Produkt erstellen", security = {@SecurityRequirement(name = "bearerAuth")})
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produkt erstellt",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validierungsfehler",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nicht autorisiert",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
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
    
        @Operation(summary = "Produkt aktualisieren", security = {@SecurityRequirement(name = "bearerAuth")})
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produkt aktualisiert",
                content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validierungsfehler",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nicht autorisiert",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
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
    
        @Operation(summary = "Produkt löschen", security = {@SecurityRequirement(name = "bearerAuth")})
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produkt gelöscht"),
            @ApiResponse(responseCode = "401", description = "Nicht autorisiert",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
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
    
        @Operation(summary = "Lagerbestand prüfen")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bestand geliefert",
                content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{productId}/stock")
    public ResponseEntity<StockResponse> checkStock(@PathVariable("productId") UUID productId) {
        log.info("GET /api/products/{}/stock - Checking stock", productId);
        StockResponse response = productService.checkStock(productId);
        return ResponseEntity.ok(response);
    }

        @Operation(summary = "Lagerbestand reduzieren (intern)")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bestand reduziert",
                content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unzureichender Bestand / Validierungsfehler",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping("/{productId}/decrease")
    public ResponseEntity<SuccessResponse> decreaseStock(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody DecreaseStockRequest request) {
        log.info("POST /api/products/{}/decrease - Decreasing stock by {}", productId, request.getQuantity());
        SuccessResponse response = productService.decreaseStock(productId, request);
        return ResponseEntity.ok(response);
    }
}