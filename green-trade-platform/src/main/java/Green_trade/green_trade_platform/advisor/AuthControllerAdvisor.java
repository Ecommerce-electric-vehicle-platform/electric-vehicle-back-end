package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.InvalidArgumentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthControllerAdvisor {
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<?> handlerForInvalidArgumentException(InvalidArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
