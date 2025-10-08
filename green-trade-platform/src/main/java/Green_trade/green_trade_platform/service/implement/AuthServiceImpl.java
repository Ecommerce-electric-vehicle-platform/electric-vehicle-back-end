package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.PasswordMismatchException;
import Green_trade.green_trade_platform.exception.UsernameException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.ForgotPasswordRequest;
import Green_trade.green_trade_platform.request.VerifyOtpForgotPasswordRequest;
import Green_trade.green_trade_platform.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private BuyerRepository buyerRepository;

    private OtpServiceImpl otpService;

    private RedisOtpService redisOtpService;

    private DelegatingPasswordEncoder passwordEncoder;

    public AuthServiceImpl(BuyerRepository buyerRepository, RedisOtpService redisOtpService, OtpServiceImpl otpService, DelegatingPasswordEncoder passwordEncoder) {
        this.buyerRepository = buyerRepository;
        this.redisOtpService = redisOtpService;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void verifyUsernameForgotPassword(String username) throws Exception {
        try {
            log.info(">>> username from request: {}", username);
            Optional<Buyer> buyerOpt = buyerRepository.findByUsername(username);
            if(buyerOpt.isEmpty()) {
                throw new UsernameException("Username is not existed");
            }
            log.info(">>> Username is existed");

            String otp = otpService.generateOtpCode();
            log.info(">>> OTP: {}", otp);

            redisOtpService.savePending(buyerOpt.get().getUsername(), buyerOpt.get().getEmail(), otp);
            otpService.sendOtpEmail(buyerOpt.get().getEmail(), otp);
        } catch (Exception e) {
            log.info(">>> Error at verifyForgotPassword: " + e);
        }
    }

    public void verifyOtpForgotPassword(VerifyOtpForgotPasswordRequest request) {
        Map<String, String> pending = redisOtpService.getPending(request.getEmail());
        if(pending == null) {
            throw new IllegalArgumentException("Invalid email or user did not forget password yet!");
        }
        log.info(">>> Passed pending is not null");

        String otp = pending.get("otp");
        log.info(">>> Otp From User: {}", request.getOtp());
        log.info(">>> Otp Redis: {}", otp);
        if(!request.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Otp are not the same!");
        }
        log.info(">>> Passed Otp matched");
        redisOtpService.deletePending(request.getEmail());
        log.info(">>> Passed delete pending on redis");
    }

    public Buyer forgotPassword(ForgotPasswordRequest request) throws Exception {
        try {
            log.info(">>> Executed forgotPassword");
            Optional<Buyer> buyerOpt = buyerRepository.findByUsername(request.getUsername());
            if(buyerOpt.isEmpty()) {
                throw new UsernameException("Username is not existed");
            }
            log.info(">>> Username is existed");

            if(!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new PasswordMismatchException();
            }
            log.info(">>> Password and Confirm Password is matched");

            String updatePassword = passwordEncoder.encode(request.getNewPassword());
            buyerOpt.get().setPassword(updatePassword);
            log.info(">>> Updated new password successfully");

            return buyerRepository.save(buyerOpt.get());
        } catch (Exception e) {
            log.info(">>> Error at forgotPassword: " + e);
            throw e;
        }
    }
}
