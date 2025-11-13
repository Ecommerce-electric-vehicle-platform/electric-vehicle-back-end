package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrdersResponse {
    private List<OrderResponse> orders; // Danh sách orders của trang hiện tại
    private Long totalOrders; // Tổng số orders (tất cả status)
    private Integer currentPage; // Trang hiện tại
    private Integer totalPages; // Tổng số trang
    private Integer pageSize; // Số items mỗi trang
}

