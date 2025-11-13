package Green_trade.green_trade_platform.response;

import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private OrderResponse orderResponse;
    private PostProductResponse postProduct;
    // Trạng thái dispute của đơn hàng: null nếu không có dispute, hoặc status của dispute nếu có
    private DisputeStatus disputeStatus;
}
