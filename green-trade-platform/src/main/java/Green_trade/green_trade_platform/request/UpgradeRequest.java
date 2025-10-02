package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpgradeRequest {
    @NotBlank(message = "Store name must not be blank.")
    private String storeName;

    @NotBlank(message = "Tax number must not be blank.")
    private String taxNumber;

    @NotBlank(message = "Identity number must not be blank.")
    private String identityNumber;
}
