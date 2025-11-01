package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.CancelOrderReason;
import Green_trade.green_trade_platform.response.CancelOrderReasonResponse;
import org.springframework.stereotype.Component;

@Component
public class CancelOrderReasonMapper {

    public CancelOrderReasonResponse toDto(CancelOrderReason cancelOrderReason) {
        return CancelOrderReasonResponse.builder()
                .id(cancelOrderReason.getId())
                .cancelOrderReasonName(cancelOrderReason.getCancelReasonName())
                .build();
    }
}
