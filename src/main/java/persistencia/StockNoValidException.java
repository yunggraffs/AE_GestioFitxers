package persistencia;

public class StockNoValidException extends RuntimeException {
    public StockNoValidException(String message) {
        super(message);
    }
}
