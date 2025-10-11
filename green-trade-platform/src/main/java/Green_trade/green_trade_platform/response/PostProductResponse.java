package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostProductResponse {
    private Long sellerId;
    private String sellerStoreName;
    private String title;
    private String brand;
    private String model;
    private Long manufactureYear;
    private String usedDuration;
    private String rejectedReason;
    private String conditionLevel;
    private boolean status;
    private String categoryName;
}
