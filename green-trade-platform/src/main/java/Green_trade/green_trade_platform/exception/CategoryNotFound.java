package Green_trade.green_trade_platform.exception;

public class CategoryNotFound extends RuntimeException {
    public CategoryNotFound(String message) {
        super(message);
    }

    public CategoryNotFound() {
        super("Category is not found");
    }
}
