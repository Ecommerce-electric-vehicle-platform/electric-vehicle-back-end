package Green_trade.green_trade_platform.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BuyerResponse {
    private Long buyerId;
    private String username;
    private String password;
    private String fullName;
    private String defaultShippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createAt;
    private String accessToken;
    private String refreshToken;
}
