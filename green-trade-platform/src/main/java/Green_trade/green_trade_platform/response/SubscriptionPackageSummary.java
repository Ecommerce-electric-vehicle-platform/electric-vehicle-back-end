package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackageSummary {
    private Long packageId;
    private String packageName;
    private String description;
    private Boolean isActive;
    private Long maxProduct;
    private Long maxImgPerPost;
    private Boolean canSendVerifyRequest;
    private Long totalSubscribers; // Tổng số sellers đã mua
    private Double totalRevenue; // Tổng doanh thu
}

