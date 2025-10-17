package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.response.AuthResponse;
import Green_trade.green_trade_platform.response.BuyerResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthResponse toDto(Buyer buyer, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .username(buyer.getUsername())
                .email(buyer.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse toDto(Admin admin, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .username(admin.getEmployeeNumber())
                .email(admin.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
