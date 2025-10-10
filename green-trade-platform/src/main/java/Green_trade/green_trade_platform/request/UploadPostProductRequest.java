package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPostProductRequest {
    private String title;
    private String brand;
    private String model;
    private String manufactureYear;
    private String usedDuration;
    private String conditionLevel;
    private double price;
    private String description;
    private String locationTrading;
    private Long categoryId;
}
