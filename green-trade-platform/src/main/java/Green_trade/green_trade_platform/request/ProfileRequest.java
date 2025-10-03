package Green_trade.green_trade_platform.request;

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
    @NotBlank(message = "Shipping address is required.")
    @Pattern(
            regexp = "^[\\p{L}0-9\\s,.\\-\\/]+$",
            message = "Shipping address contains invalid characters."
    )
    private String defaultShippingAddress;

    @NotBlank(message = "Full name is required.")
    @Pattern(
            regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$",
            message = "Full name can only include letters and spaces."
    )
    private String fullName;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;
}
