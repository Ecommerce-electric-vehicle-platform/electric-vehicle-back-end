package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.SignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;
import jakarta.mail.MessagingException;

public interface SignupService {
    void startSignUp(SignUpRequest request);
    Buyer verifyOtp(VerifyOtpRequest request);
    void sendOtpEmail(String to, String otp) throws MessagingException;
}
