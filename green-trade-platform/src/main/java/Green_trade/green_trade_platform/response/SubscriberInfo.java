package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberInfo {
    private Long sellerId;
    private String sellerName;
    private String storeName;
    private Long subscriptionId;
    private Double priceAtPurchase;
    private LocalDateTime startDay;
    private LocalDateTime endDay;
    private Boolean isActive;
}

