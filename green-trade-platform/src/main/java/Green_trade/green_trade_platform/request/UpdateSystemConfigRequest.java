package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSystemConfigRequest {
    @NotBlank(message = "Config value is required.")
    @Pattern(
            regexp = "^\\d+$",
            message = "Config value must be a positive number."
    )
    private String configValue;
}

