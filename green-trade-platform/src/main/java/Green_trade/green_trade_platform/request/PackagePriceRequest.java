package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagePriceRequest {
    private Long id; // Optional: có id nếu là update, null nếu là create mới

    @NotNull(message = "Price is required.")
    @Positive(message = "Price must be positive.")
    private Double price;

    // isActive is optional: if null when updating, keep existing value; if null when creating, default to true
    private Boolean isActive;

    @NotNull(message = "durationByDay is required.")
    @Positive(message = "durationByDay must be positive.")
    private Long durationByDay;

    @NotBlank(message = "Currency is required.")
    private String currency;

    @NotNull(message = "discountPercent is required.")
    @PositiveOrZero(message = "discountPercent must be positive or zero.")
    private Double discountPercent;
}

