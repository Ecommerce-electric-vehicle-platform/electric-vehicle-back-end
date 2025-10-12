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
public class BuyerResponse {
    private Long buyerId;
    private String username;
    private String fullName;
    private String defaultShippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
