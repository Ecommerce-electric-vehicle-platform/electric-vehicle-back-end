package Green_trade.green_trade_platform.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionPackageRequest {
    @NotBlank(message = "Package name is required.")
    private String name;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "isActive is required.")
    private Boolean isActive;

    @NotNull(message = "maxProduct is required.")
    @Positive(message = "maxProduct must be positive.")
    private Long maxProduct;

    @NotNull(message = "maxImgPerPost is required.")
    @Positive(message = "maxImgPerPost must be positive.")
    private Long maxImgPerPost;

    @NotNull(message = "canSendVerifyRequest is required.")
    private Boolean canSendVerifyRequest;

    @Valid
    private List<PackagePriceRequest> prices; // Optional: danh sách giá của gói (có id để update, null id để create mới)
}

