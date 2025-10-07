package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.DuplicateProfileException;
import Green_trade.green_trade_platform.exception.EmailException;
import Green_trade.green_trade_platform.exception.ProfileNotFoundException;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class BuyerControllerAdvisor {
    @Autowired
    private ResponseMapper responseMapper;

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<?> handleProfileNotFoundException(ProfileNotFoundException e, HttpServletRequest request) {
        log.info(">>> Exception message of profile not found: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("error", "Bad Request");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", e.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DuplicateProfileException.class)
    public ResponseEntity<?> handleProfileNotFoundException(DuplicateProfileException e, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("error", "Bad Request");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", e.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<RestResponse<Object, Object>> handleEmailException(EmailException e) {
        RestResponse<Object, Object> response = responseMapper.toDto(
                false,
                "Email Exception",
                null,
                Map.of(
                        "origin", e.getStackTrace()[0].toString(),
                        "message", e.getMessage(),
                        "errorType", e.getClass().getSimpleName()
                )
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
