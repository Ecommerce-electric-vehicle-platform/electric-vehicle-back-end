package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeedVerifyPostRequest {
    @NotBlank
    private int size;
    @NotBlank
    private int page;
}
