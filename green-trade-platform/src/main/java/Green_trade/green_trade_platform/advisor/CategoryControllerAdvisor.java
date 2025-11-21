package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.CategoryNotFound;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class CategoryControllerAdvisor {

    private final ResponseMapper responseMapper;

    public CategoryControllerAdvisor(ResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
    }

    @ExceptionHandler(CategoryNotFound.class)
    public ResponseEntity<RestResponse<Object, Map<String, Object>>> handleCategoryNotFound(CategoryNotFound e) {
        Map<String, Object> responseData = Map.of(
                "origin", e.getStackTrace()[0].toString(),
                "message", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
        );
        RestResponse<Object, Map<String, Object>> response = responseMapper.toDto(
                false,
                "CATEGORY NOT FOUND",
                null,
                responseData
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(response);
    }
}
