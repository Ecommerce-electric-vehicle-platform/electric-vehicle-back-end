package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.exception.EmailException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.SignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    public void startSignUp(SignUpRequest request) {
        if(repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Duplicate email!");
        }
        // Create OTP
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        String hashPassword = passwordEncoder.encode(request.getPassword());
        // Save user temporary in Redis for verifying OTP via email
        // User send request to sign up -> create OTP + save user to Redis -> user send verify OTP -> save user into database
        otpService.savePendingBuyer(request.getUsername(), hashPassword, request.getEmail(), otp);
        sendOtpEmail(request.getEmail(), otp);
    }

    // Verify otp
    @Override
    public Buyer verifyOtp(VerifyOtpRequest request) {
        // Get pending buyer in Redis
        Map<String, String> pending = otpService.getPendingBuyer(request.getEmail());
        if(pending == null) {
            throw new IllegalArgumentException("Invalid email or user did not sign up yet!");
        }
        // Get OTP in map
        String otp = pending.get("otp");
        if(!request.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Otp are not the same!");
        }

        Buyer buyer = Buyer.builder()
                .username(pending.get("username"))
                .password(pending.get("password"))
                .email(request.getEmail())
                .build();
        otpService.deletePendingBuyer(request.getEmail());
        return repository.save(buyer);
    }

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Green Trade Platform - Email Verification OTP");

            String htmlContent = """
                <html>
                    <body style="font-family: Arial, sans-serif; line-height:1.6;">
                        <h2>Welcome to Green Trade Platform 🌱</h2>
                        <p>Dear user,</p>
                        <p>Thank you for signing up! Please use the following OTP code to verify your email:</p>
                        <h1 style="color: #2E8B57; letter-spacing: 4px;">%s</h1>
                        <p>This OTP is valid for <strong>10 minutes</strong>. 
                        Do not share it with anyone for security reasons.</p>
                        <br/>
                        <p>Best regards,<br/>Green Trade Team</p>
                    </body>
                </html>
                """.formatted(otp);

            helper.setText(htmlContent, true); // true => gửi dạng HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send OTP email");
        }
    }
}
