package ch.tbz.products.exceptions;

public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String productName, Integer available, Integer requested) {
        super(String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d", 
            productName, available, requested));
    }
}
