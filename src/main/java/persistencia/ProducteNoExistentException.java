package persistencia;

public class ProducteNoExistentException extends RuntimeException {
  public ProducteNoExistentException(String message) {
    super(message);
  }
}
