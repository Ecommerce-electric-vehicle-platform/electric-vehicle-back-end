package Green_trade.green_trade_platform.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestResponse<T, E> {
    private boolean success;
    private String message;
    private T data;
    private E error;
}
