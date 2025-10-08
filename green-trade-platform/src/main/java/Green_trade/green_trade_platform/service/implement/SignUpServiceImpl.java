package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.exception.EmailException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.SignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;
import Green_trade.green_trade_platform.service.SignUpService;
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
public class SignUpServiceImpl implements SignUpService {
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
        log.info(">>> OTP: {}", otp);
        String hashPassword = passwordEncoder.encode(request.getPassword());

        // Save user temporary in Redis for verifying OTP via email
        // User send request to sign up -> create OTP + save user to Redis -> user send verify OTP -> save user into database
        otpService.savePendingBuyer(request.getUsername(), hashPassword, request.getEmail(), otp);
        log.info(">>> Save pending buyer successfully.");
        sendOtpEmail(request.getEmail(), otp);
        log.info(">>> Send otp successfully.");
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
        <!doctype html>
        <html lang="vi">
          <body style="margin:0; padding:0; background-color:#f5f7fa; font-family:Arial,Helvetica,sans-serif; line-height:1.6;">
            <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="padding:28px 0; background:#f5f7fa;">
              <tr>
                <td align="center">
                  <table role="presentation" cellpadding="0" cellspacing="0" width="560" style="background:#ffffff; border-radius:12px; overflow:hidden;">
                    <tr>
                      <td style="background:#2e7d32; color:#ffffff; text-align:center; padding:18px 24px;">
                        <div style="font-size:18px; font-weight:700;">Green Trade</div>
                        <div style="font-size:12px; opacity:.9;">Xe điện & Pin xe điện</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:26px 28px;">
                        <div style="font-size:16px; color:#111827; margin-bottom:10px;">Chào mừng bạn!</div>
                        <div style="font-size:14px; color:#4b5563; line-height:1.6;">
                          Vui lòng dùng mã OTP bên dưới để xác minh email của bạn:
                        </div>

                        <div style="text-align:center; margin:22px 0 10px;">
                          <div style="display:inline-block; background:#f0fdf4; border:1px solid #ccf1d8; border-radius:10px; padding:14px 24px;">
                            <span style="font-size:28px; letter-spacing:6px; color:#1b5e20; font-weight:700;">
                              %s
                            </span>
                          </div>
                        </div>

                        <div style="font-size:13px; color:#6b7280; text-align:center;">
                          Mã có hiệu lực <strong>10 phút</strong>. Không chia sẻ mã để bảo mật tài khoản.
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#fafafa; border-top:1px solid #f0f0f0; padding:16px 24px; text-align:center; color:#9ca3af; font-size:12px;">
                        © 2025 Green Trade • Năng lượng sạch cho hành trình bền vững
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
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
