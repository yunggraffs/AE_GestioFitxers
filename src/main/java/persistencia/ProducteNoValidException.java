package persistencia;

public class ProducteNoValidException extends RuntimeException {
    public ProducteNoValidException(String message) {
        super(message);
    }
}
