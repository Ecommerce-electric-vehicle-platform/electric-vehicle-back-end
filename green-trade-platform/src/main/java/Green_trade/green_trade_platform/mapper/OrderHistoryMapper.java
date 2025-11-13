package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.repository.DisputeRepository;
import Green_trade.green_trade_platform.response.OrderHistoryResponse;
import Green_trade.green_trade_platform.response.OrderResponse;
import Green_trade.green_trade_platform.response.PostProductResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class OrderHistoryMapper {

    private final OrderMapper orderMapper;
    private final PostProductMapper postProductMapper;
    private final DisputeRepository disputeRepository;

    public OrderHistoryResponse toDto(Order order) {
        // Kiểm tra xem order có dispute không
        DisputeStatus disputeStatus = null;
        try {
            List<Dispute> disputes = disputeRepository.findByOrder_Id(order.getId());
            if (disputes != null && !disputes.isEmpty()) {
                // Lấy dispute mới nhất (sắp xếp theo createdAt giảm dần)
                Dispute latestDispute = disputes.stream()
                        .max((d1, d2) -> {
                            if (d1.getCreatedAt() == null && d2.getCreatedAt() == null) return 0;
                            if (d1.getCreatedAt() == null) return -1;
                            if (d2.getCreatedAt() == null) return 1;
                            return d1.getCreatedAt().compareTo(d2.getCreatedAt());
                        })
                        .orElse(disputes.get(0));
                disputeStatus = latestDispute.getStatus();
            }
        } catch (Exception e) {
            // Nếu có lỗi khi query dispute, để disputeStatus = null
            // Không throw exception để không ảnh hưởng đến response chính
        }

        return OrderHistoryResponse.builder()
                .orderResponse(orderMapper.toDto(order))
                .postProduct(postProductMapper.toDto(order.getPostProduct()))
                .disputeStatus(disputeStatus)
                .build();
    }
}
