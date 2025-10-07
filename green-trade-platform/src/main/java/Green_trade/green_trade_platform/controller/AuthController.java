package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.AuthMapper;
import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.*;
import Green_trade.green_trade_platform.response.AuthResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.AuthServiceImpl;
import Green_trade.green_trade_platform.service.implement.RedisTokenService;
import Green_trade.green_trade_platform.service.implement.SignInServiceImpl;
import Green_trade.green_trade_platform.service.implement.SignUpServiceImpl;
import Green_trade.green_trade_platform.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private SignInServiceImpl signInService;
    @Autowired
    private SignUpServiceImpl service;
    @Autowired
    private BuyerMapper buyerMapper;
    @Autowired
    private ResponseMapper responseMapper;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisTokenService redisTokenService;
    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private AuthMapper authMapper;


    private final long REFRESH_EXPIRE_TIME = 7L * 24 * 60 * 60 * 1000; // 7 days
    private final long ACCESS_EXPIRE_TIME = 15 * 60 * 1000; // 15 minutes

    @Operation(summary = "Register for new customer",
            description = "Return response show that register successfully!")
    @PostMapping("/signup")
    public ResponseEntity<RestResponse<Object, Object>> signUp(@Valid @RequestBody SignUpRequest req) {
        service.startSignUp(req);
        return ResponseEntity.ok(responseMapper.toDto(
                true, "Sent OTP to email", null, null
        ));
    }

    @Operation(summary = "Sign in for customer",
                description = "Return response show that user has signed in successfully")
    @PostMapping("/signin")
    public ResponseEntity<RestResponse<AuthResponse, Object>>  signIn(@Valid @RequestBody SignInRequest req) {
        Buyer user = signInService.startSignIn(req);

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), ACCESS_EXPIRE_TIME);
        String refreshToken = jwtUtils.generateTokenFromUsername(user.getUsername(), REFRESH_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(user.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        AuthResponse authResponse = authMapper.toDto(user, accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.OK.value())
                .body(responseMapper.toDto(
                        true, "LOGIN SUCCESSFULLY", authResponse, null
                ));
    }

    @Operation(summary = "Sign in with Google for customer",
                            description = "Return response show that user has signed in successfully")
    @PostMapping("/signin-google")
    public ResponseEntity<RestResponse<AuthResponse, Object>> loginWithGoogle(@RequestBody SignInGoogleRequest body) throws Exception {
        Buyer user = signInService.startSignInWithGoogle(body);

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), ACCESS_EXPIRE_TIME);
        String refreshToken = jwtUtils.generateTokenFromUsername(user.getUsername(), REFRESH_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(user.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        AuthResponse authResponse = authMapper.toDto(user, accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.OK.value())
                .body(responseMapper.toDto(
                        true, "SIGN IN SUCCESSFULLY", authResponse, null
                ));
    }

    @Operation(summary = "Verify Username Forgot Password",
                description = "Return response show that verify username forgot password request successfully")
    @PostMapping("/verify-username-forgot-password")
    public ResponseEntity<RestResponse<Object, Object>> verifyForgotPassword(@RequestBody VerifyUsernameForgotPasswordRequest req) throws Exception {
        authService.verifyUsernameForgotPassword(req.getUsername());
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true, "OTP Sent To Email", null, null
        ));
    }

    @Operation(summary = "Verify OTP Forgot Password",
                description = "Return response show thât verify OTP forgot password request successfully")
    @PostMapping("/verify-otp-forgot-password")
    public ResponseEntity<RestResponse<Object, Object>> verifyOtpForgotPassword(@RequestBody VerifyOtpForgotPasswordRequest request) {
        log.info(">>> We are at verifyOtpForgotPassword");
        authService.verifyOtpForgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true, "Verified OTP Successfully", null, null
        ));
    }

    @Operation(summary = "Forgot Password API",
                        description = "Return response show that new password is updated")
    @PostMapping("/forgot-password")
    public ResponseEntity<RestResponse<Buyer, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) throws Exception {
        Buyer result = authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true,
                "UPDATED PASSWORD SUCCESSFULLY",
                result,
                null
        ));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify otp via email",
                description = "Return verify email.")
    public ResponseEntity<RestResponse<AuthResponse, Object>> verify(@Valid @RequestBody VerifyOtpRequest req) {
        Buyer buyer = service.verifyOtp(req);
        String refreshToken = jwtUtils.generateTokenFromUsername(buyer.getUsername(), REFRESH_EXPIRE_TIME);
        String accessToken = jwtUtils.generateTokenFromUsername(buyer.getUsername(), ACCESS_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(buyer.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        AuthResponse authResponse = authMapper.toDto(buyer, accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true, "SIGN UP SUCCESSFULLY", authResponse, null
        ));
    }
}
