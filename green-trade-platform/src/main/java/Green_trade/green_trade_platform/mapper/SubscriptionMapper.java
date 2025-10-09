package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.response.SubscriptionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SubscriptionMapper {
    public SubscriptionResponse toDto(boolean valid, LocalDateTime expiryDate, String packageName) {
        return SubscriptionResponse.builder()
                .valid(valid)
                .expiryDate(expiryDate)
                .packageName(packageName)
                .build();
    }
}
