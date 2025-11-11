package Green_trade.green_trade_platform.response;

import Green_trade.green_trade_platform.enumerate.SystemWalletStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemWalletResponse {
    private long id;
    private long buyerWalletId;
    private long sellerWalletId;
    private String concurrency;
    private BigDecimal balance;
    private BigDecimal shippingFee;
    private SystemWalletStatus status;
    private LocalDateTime createdAt;
}
