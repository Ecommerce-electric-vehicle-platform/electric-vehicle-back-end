package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeRequest {
    @NotBlank(message = "Store name is required.")
    private String storeName;

    @NotBlank(message = "Tax number is required.")
    private String taxNumber;

    @NotBlank(message = "Identity number is required.")
    private String identityNumber;
}
