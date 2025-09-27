package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.Mapper.BuyerMapper;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;
import Green_trade.green_trade_platform.service.SignupServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class SignUpController {
    @Autowired
    private SignupServiceImpl service;
    @Autowired
    private BuyerMapper mapper;

    @Operation(summary = "Register for new customer",
            description = "Return response show that register successfully!")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UsernamePasswordSignUpRequest req) {
        service.startSignUp(req);
        return ResponseEntity.ok(Map.of("message", "OTP đã gửi đến email"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify otp via email",
                description = "Return verify email.")
    public ResponseEntity<?> verify(@RequestBody VerifyOtpRequest req) {
        service.verifyOtp(req);
        return ResponseEntity.ok(Map.of("message", "Xác thực thành công"));
    }
}
