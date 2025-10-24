package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PostProductNotFoundAdvisor {

    private final ResponseMapper responseMapper;

    public PostProductNotFoundAdvisor(ResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
    }

    @ExceptionHandler(PostProductNotFound.class)
    public ResponseEntity<RestResponse<?, ?>> handlePostProductNotFoundException(PostProductNotFound e) {
        RestResponse<Object, Map<String, String>> response = responseMapper.toDto(
                false,
                "POST PRODUCT NOT FOUND",
                null,
                Map.of(
                        "origin", e.getStackTrace()[0].toString(),
                        "message", e.getMessage(),
                        "errorType", e.getClass().getSimpleName()
                )
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
    }
}
