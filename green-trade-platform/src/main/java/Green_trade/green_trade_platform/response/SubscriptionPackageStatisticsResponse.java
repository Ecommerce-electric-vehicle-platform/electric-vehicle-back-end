package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackageStatisticsResponse {
    private Long packageId;
    private String packageName;
    private String description;
    private Boolean isActive;
    private Long totalSubscribers; // Tổng số sellers đã mua gói này
    private BigDecimal totalRevenue; // Tổng doanh thu từ gói này
    private List<SubscriberInfo> subscribers; // Danh sách sellers đã mua (optional, có thể null nếu không cần)
}

