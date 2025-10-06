package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.UsernameException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.VerifyOtpForgotPassword;
import Green_trade.green_trade_platform.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private BuyerRepository buyerRepository;

    private OtpServiceImpl otpService;

    private RedisOtpService redisOtpService;

    public AuthServiceImpl(BuyerRepository buyerRepository, RedisOtpService redisOtpService, OtpServiceImpl otpService) {
        this.buyerRepository = buyerRepository;
        this.redisOtpService = redisOtpService;
        this.otpService = otpService;
    }

    @Override
    public void verifyUsernameForgotPassword(String username) throws Exception {
        try {
            log.info(">>> username from request: {}", username);
            Optional<Buyer> buyerOpt = buyerRepository.findByUsername(username);
            if(buyerOpt.isEmpty()) {
                throw new UsernameException("Username is not existed");
            }

            String otp = otpService.generateOtpCode();
            log.info(">>> OTP: {}", otp);

            redisOtpService.savePending(buyerOpt.get().getUsername(), buyerOpt.get().getEmail(), otp);
            otpService.sendOtpEmail(buyerOpt.get().getEmail(), otp);
        } catch (Exception e) {
            log.info(">>> Error at verifyForgotPassword: " + e);
        }
    }

    public void verifyOtpForgotPassword(VerifyOtpForgotPassword request) {
        Map<String, String> pending = redisOtpService.getPending(request.getEmail());
        if(pending == null) {
            throw new IllegalArgumentException("Invalid email or user did not forget password yet!");
        }
        log.info(">>> Passed pending is not null");

        String otp = pending.get("otp");
        log.info(">>> Otp Redis: {}", otp);
        log.info(">>> Otp User: {}", request.getOtp());
        if(!request.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Otp are not the same!");
        }
        log.info(">>> Passed Otp matched");
        redisOtpService.deletePending(request.getEmail());
        log.info(">>> Passed delete pending on redis");
    }

    // Verify otp
//    public Buyer verifyForgotPasswordOtp(VerifyOtpRequest request) {
//        // Get pending buyer in Redis
//        Map<String, String> pending = redisOtpService.getPendingBuyer(request.getEmail());
//        if(pending == null) {
//            throw new IllegalArgumentException("Invalid email or user did not sign up yet!");
//        }
//        // Get OTP in map
//        String otp = pending.get("otp");
//        if(!request.getOtp().equals(otp)) {
//            throw new IllegalArgumentException("Otp are not the same!");
//        }
//        redisOtpService.deletePendingBuyer(request.getEmail());
//        return repository.save(buyer);
//    }
}
