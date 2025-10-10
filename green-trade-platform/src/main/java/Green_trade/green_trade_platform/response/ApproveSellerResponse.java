package Green_trade.green_trade_platform.response;

import Green_trade.green_trade_platform.enumerate.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerResponse {
    private Long sellerId;
    private SellerStatus decision;
    private String reason;
    private LocalDateTime decidedAt;
}
