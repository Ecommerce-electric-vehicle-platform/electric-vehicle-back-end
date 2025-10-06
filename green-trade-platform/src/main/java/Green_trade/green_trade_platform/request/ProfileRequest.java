package Green_trade.green_trade_platform.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProfileRequest {
    @NotBlank(message = "Full name is required.")
    @Pattern(
            regexp = "^[\\p{L}\\s]+$",
            message = "Full name can only include letters and spaces."
    )
    private String fullName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Phone number must start with 0 and contain 10 digits."
    )
    private String phoneNumber;

    @NotBlank(message = "Shipping address is required.")
    @Pattern(
            regexp = "^[\\p{L}0-9\\s,./-]+$",
            message = "Shipping address contains invalid characters."
    )
    private String defaultShippingAddress;
}
