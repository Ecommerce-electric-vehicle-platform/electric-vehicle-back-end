package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;

public interface SignupService {
    void startSignUp(UsernamePasswordSignUpRequest request);
    void verifyOtp(VerifyOtpRequest request);
    void sendOtpEmail(String to, String otp);
}
