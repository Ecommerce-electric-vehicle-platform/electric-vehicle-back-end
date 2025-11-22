package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Manufacture Year is required")
    @Min(value = 1900, message = "Manufacture Year must be later than 1900")
    @Max(value = 2100, message = "Manufacture Year seems invalid")
    private Long manufactureYear;

    @NotBlank(message = "Used Duration is required")
    private String usedDuration;

    @NotBlank(message = "Condition Level is required")
    private String conditionLevel;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Length is required")
    private String length;

    @NotBlank(message = "Width is required")
    private String width;

    @NotBlank(message = "Height is required")
    private String height;

    @NotBlank(message = "Weight is required")
    private String weight;

    @NotBlank(message = "Location Trading is required")
    private String locationTrading;

    @NotNull(message = "Category Id is required")
    @Positive(message = "Category Id must be positive")
    private Long categoryId;
}
