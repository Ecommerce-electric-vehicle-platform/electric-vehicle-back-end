package Green_trade.green_trade_platform.request;

import Green_trade.green_trade_platform.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for placing a new order")
public class PlaceOrderRequest {
    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be a positive number")
    @Schema(
            description = "ID of the product to order",
            example = "123",
            required = true
    )
    private Long postProductId;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(
            description = "Username of the buyer placing the order",
            example = "buyer123",
            required = true,
            minLength = 3,
            maxLength = 50
    )
    private String username;

    @Schema(
            description = "Full name of the recipient for shipping",
            example = "Nguyễn Văn A",
            required = false
    )
    private String fullName;

    @NotBlank(message = "Street cannot be blank")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    @Schema(
            description = "Street address for shipping",
            example = "123 Đường ABC",
            required = true,
            maxLength = 255
    )
    private String street;

    @Schema(
            description = "Ward name for shipping address",
            example = "Phường 1",
            required = false
    )
    private String wardName;

    @Schema(
            description = "District name for shipping address",
            example = "Quận 1",
            required = false
    )
    private String districtName;

    @Schema(
            description = "Province name for shipping address",
            example = "TP. Hồ Chí Minh",
            required = false
    )
    private String provinceName;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9,10}$",
            message = "Phone number must be valid (starts with 0 or +84 and has 10–11 digits)"
    )
    @Schema(
            description = "Phone number of the recipient (Vietnamese format: starts with 0 or +84, 10-11 digits)",
            example = "0912345678",
            required = true,
            pattern = "^(0|\\+84)[0-9]{9,10}$"
    )
    private String phoneNumber;

    @NotNull(message = "Shipping partner ID cannot be null")
    @Positive(message = "Shipping partner ID must be a positive number")
    @Schema(
            description = "ID of the shipping partner (e.g., GHN, GHTK)",
            example = "1",
            required = true
    )
    private Long shippingPartnerId;

    @NotNull(message = "Payment ID cannot be null")
    @Positive(message = "Payment ID must be a positive number")
    @Schema(
            description = "ID of the payment method (1 = COD, 2 = Wallet/Online Payment)",
            example = "1",
            required = true
    )
    private Long paymentId;
}
