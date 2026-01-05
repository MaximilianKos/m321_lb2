package ch.tbz.products.exceptions;

public class ProductValidationException extends RuntimeException {
    
    public ProductValidationException(String message) {
        super(message);
    }
}
