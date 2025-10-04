package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.JwtException;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class JwtControllerAdvisor {
    private final ResponseMapper responseMapper;

    public JwtControllerAdvisor(ResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<RestResponse<Object, JwtException>> handleJwtException(JwtException ex, HttpServletRequest request) {
        RestResponse<Object, JwtException> response = responseMapper.toDto(
                false,
                "Unauthorized",
                null,
                ex
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
