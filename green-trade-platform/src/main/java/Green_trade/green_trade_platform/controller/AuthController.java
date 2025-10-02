package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.SignInRequest;
import Green_trade.green_trade_platform.request.SignUpRequest;
import Green_trade.green_trade_platform.request.VerifyOtpRequest;
import Green_trade.green_trade_platform.response.BuyerResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.RedisTokenService;
import Green_trade.green_trade_platform.service.implement.SignInServiceImpl;
import Green_trade.green_trade_platform.service.implement.SignupServiceImpl;
import Green_trade.green_trade_platform.util.GoogleVerifierService;
import Green_trade.green_trade_platform.util.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private SignInServiceImpl signInService;
    @Autowired
    private SignupServiceImpl service;
    @Autowired
    private BuyerMapper mapper;
    @Autowired
    private ResponseMapper responseMapper;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisTokenService redisTokenService;

    private final long REFRESH_EXPIRE_TIME = System.currentTimeMillis() + + 7L * 24 * 60 * 60 * 1000; // 7 days
    private final long ACCESS_EXPIRE_TIME = 15 * 60 * 1000; // 15 minutes

    @Operation(summary = "Register for new customer",
            description = "Return response show that register successfully!")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest req) {
        service.startSignUp(req);
        return ResponseEntity.ok(Map.of("message", "Send OTP to email."));
    }

    @Operation(summary = "Sign in for customer",
                description = "Return response show that user has signed in successfully")
    @PostMapping("/signin")
    public ResponseEntity<RestResponse<BuyerResponse, Object>>  signin(@Valid @RequestBody SignInRequest req) {
        Buyer user = signInService.startSignIn(req);

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), ACCESS_EXPIRE_TIME);
        String refreshToken = jwtUtils.generateTokenFromUsername(user.getUsername(), REFRESH_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(user.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        BuyerResponse buyerResponse = mapper.toDto(user, accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.OK.value())
                .body(responseMapper.toDto(true, "LOGIN SUCCESSFULLY", buyerResponse, null));
    }

    @Operation(summary = "Sign in with Google for customer",
                            description = "Return response show that user has signed in successfully")
    @PostMapping("/google")
    public ResponseEntity<RestResponse<BuyerResponse, Object>> loginWithGoogle(@RequestBody Map<String, String> body) throws Exception {
        Buyer user = signInService.startSignInWithGoogle(body);

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), ACCESS_EXPIRE_TIME);
        String refreshToken = jwtUtils.generateTokenFromUsername(user.getUsername(), REFRESH_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(user.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        BuyerResponse buyerResponse = mapper.toDto(user, accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.OK.value())
                .body(responseMapper.toDto(true, "SIGN UP SUCCESSFULLY", buyerResponse, null));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify otp via email",
                description = "Return verify email.")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyOtpRequest req) {
        Buyer buyer = service.verifyOtp(req);
        String refreshToken = jwtUtils.generateTokenFromUsername(buyer.getUsername(), REFRESH_EXPIRE_TIME);
        String accessToken = jwtUtils.generateTokenFromUsername(buyer.getUsername(), ACCESS_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(buyer.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);
        return ResponseEntity.ok(mapper.toDto(buyer,
                        accessToken,
                        refreshToken));
    }
}
