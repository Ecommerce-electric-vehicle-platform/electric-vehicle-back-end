package Green_trade.green_trade_platform.request;

import Green_trade.green_trade_platform.enumerate.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerRequest {
    private Long adminId;
    private Long sellerId;
    private SellerStatus decision;
}
