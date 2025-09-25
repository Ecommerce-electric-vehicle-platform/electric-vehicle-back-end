package Green_trade.green_trade_platform.Mapper;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import org.springframework.stereotype.Component;

@Component
public class BuyerMapper {
    public Buyer toEntity(UsernamePasswordSignUpRequest request) {
        return Buyer.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .build();
    }
}
