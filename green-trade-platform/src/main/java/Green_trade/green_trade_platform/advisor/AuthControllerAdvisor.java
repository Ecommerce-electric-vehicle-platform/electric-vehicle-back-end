package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.AuthException;
import Green_trade.green_trade_platform.exception.InvalidArgumentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AuthControllerAdvisor {
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<?> handlerForInvalidArgumentException(InvalidArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> handlerForAuthException(AuthException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Unauthorized");
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }
}
