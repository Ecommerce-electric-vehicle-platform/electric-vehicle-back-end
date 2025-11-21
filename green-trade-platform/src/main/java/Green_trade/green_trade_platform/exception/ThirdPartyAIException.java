package Green_trade.green_trade_platform.exception;

public class ThirdPartyAIException extends RuntimeException {
    public ThirdPartyAIException(String message) {
        super(message);
    }

    public ThirdPartyAIException(String message, Throwable cause) {
        super(message, cause);
    }
}

