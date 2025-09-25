package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.response.SignUpResponse;

public interface AuthService {
    Buyer signUp(UsernamePasswordSignUpRequest request);
}
