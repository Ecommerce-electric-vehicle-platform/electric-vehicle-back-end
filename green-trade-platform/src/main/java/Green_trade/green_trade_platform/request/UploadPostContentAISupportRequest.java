package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPostContentAISupportRequest {
    private String title;
    private String brand;
    private String model;
    private Long manufactureYear;
    private String usedDuration;
    private String rejectedReason;
    private String conditionLevel;
    private BigDecimal price;
    private String length;

    private String width;

    private String height;

    private String weight;

    @NotBlank(message = "Location Trading is required")
    private String locationTrading;

    @NotNull(message = "Category Id is required")
    @Positive(message = "Category Id must be positive")
    private Long categoryId;
}
