package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.Mapper.BuyerMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;


@Service
@Slf4j
public class SignupServiceImpl implements SignupService {
    @Autowired
    private BuyerRepository repository;
    @Autowired
    private RedisOtpService otpService;
    @Autowired
    private BuyerMapper mapper;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private DelegatingPasswordEncoder passwordEncoder;

    // Starting sign up: saving buyer to redis and sending otp
    @Override
    public void startSignUp(UsernamePasswordSignUpRequest request) {
        if(repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Duplicate email!");
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        String hashPassword = passwordEncoder.encode(request.getPassword());
        sendOtpEmail(request.getEmail(), otp);
    }

    // Verify otp
    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        Map<String, String> pending = otpService.getPendingBuyer(request.getEmail());
        if(pending == null) {
            throw new IllegalArgumentException("Invalid email or user did not sign up yet!");
        }
        String otp = pending.get(request.getEmail());
        if(!request.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Otp are not the same!");
        }

        Buyer buyer = Buyer.builder()
                .username(pending.get("username"))
                .password(pending.get("password"))
                .email(request.getEmail())
                .build();
        repository.save(buyer);
        otpService.deletePendingBuyer(request.getEmail());
    }

    @Override
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Verify OTP to sign up green-trade-platform");
        msg.setText("Your OTP: " + otp + " Have TTL in 10 minutes.");
        mailSender.send(msg);
    }
}
