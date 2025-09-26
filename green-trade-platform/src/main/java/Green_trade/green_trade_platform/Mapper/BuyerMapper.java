package Green_trade.green_trade_platform.Mapper;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.response.SignUpResponse;
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

    public SignUpResponse toDto(Buyer buyer) {
        return SignUpResponse.builder().
                buyerId(buyer.getBuyerId())
                .username(buyer.getUsername())
                .password(buyer.getPassword())
                .fullName(buyer.getFullName())
                .defaultShippingAddress(buyer.getDefaultShippingAddress())
                .phoneNumber(buyer.getPhoneNumber())
                .email(buyer.getEmail())
                .createAt(buyer.getCreateAt())
                .build();
    }
}
