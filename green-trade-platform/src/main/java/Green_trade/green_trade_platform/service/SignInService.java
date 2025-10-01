package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.SignInRequest;

public interface SignInService {
    Buyer startSignIn(SignInRequest request);
}
