package Green_trade.green_trade_platform.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsernamePasswordSignUpRequest {
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String email;
}
