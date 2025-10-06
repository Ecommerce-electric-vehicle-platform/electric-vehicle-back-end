package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.EmailException;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvisor {
    @Autowired
    private ResponseMapper responseMapper;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object, Exception>> handleException(Exception ex) {
        RestResponse<Object, Exception> response = responseMapper.toDto(
                false,
                "Internal Server Error",
                null,
                ex
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<RestResponse<Object, EmailException>> handleEmailException(EmailException e) {
        RestResponse<Object, EmailException> response = responseMapper.toDto(
                false,
                "Email Exception",
                null,
                e);
        return ResponseEntity.internalServerError().body(response);
    }
}
