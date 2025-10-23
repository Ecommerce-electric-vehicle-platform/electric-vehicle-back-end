package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.AuthMapper;
import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.*;
import Green_trade.green_trade_platform.response.AuthResponse;
import Green_trade.green_trade_platform.response.BuyerResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.AuthServiceImpl;
import Green_trade.green_trade_platform.service.implement.RedisTokenService;
import Green_trade.green_trade_platform.service.implement.SignInServiceImpl;
import Green_trade.green_trade_platform.service.implement.SignUpServiceImpl;
import Green_trade.green_trade_platform.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final BuyerMapper buyerMapper;
    private SignInServiceImpl signInService;
    private SignUpServiceImpl signUpService;
    private ResponseMapper responseMapper;
    private JwtUtils jwtUtils;
    private RedisTokenService redisTokenService;
    private AuthServiceImpl authService;
    private AuthMapper authMapper;

    public AuthController (
            SignInServiceImpl signInService,
            SignUpServiceImpl signUpService,
            ResponseMapper responseMapper,
            JwtUtils jwtUtils,
            RedisTokenService redisTokenService,
            AuthServiceImpl authService,
            AuthMapper authMapper, BuyerMapper buyerMapper) {
        this.signInService = signInService;
        this.signUpService = signUpService;
        this.responseMapper = responseMapper;
        this.jwtUtils = jwtUtils;
        this.redisTokenService = redisTokenService;
        this.authService = authService;
        this.authMapper = authMapper;
        this.buyerMapper = buyerMapper;
    }


    private final long REFRESH_EXPIRE_TIME = 7L * 24 * 60 * 60 * 1000; // 7 days
    private final long ACCESS_EXPIRE_TIME = 15 * 60 * 1000; // 15 minutes
//    private final long ACCESS_EXPIRE_TIME = 30 * 1000; // 30 seconds

    @Operation(
            summary = "Register for new customer",
            description = "Return response show that register successfully!"
    )
    @PostMapping("/signup")
    public ResponseEntity<RestResponse<Object, Object>> signUp(@Valid @RequestBody SignUpRequest req) {
        signUpService.startSignUp(req);
        return ResponseEntity.ok(responseMapper.toDto(
                true, "Sent OTP to email", null, null
        ));
    }

    @Operation(
            summary = "Sign in for Admin",
                description = "Return response show that user has signed in successfully"
    )
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

    @Operation(
            summary = "Sign in for Admin",
            description = "Return response show that admin has signed in successfully"
    )
    @PostMapping("/admin/signin")
    public ResponseEntity<RestResponse<AuthResponse, Object>>  signInAdmin(@Valid @RequestBody SignInAdminRequest req) {
        Admin user = signInService.startSignInAdmin(req);

        String accessToken = jwtUtils.generateTokenFromUsername(user.getEmployeeNumber(), ACCESS_EXPIRE_TIME);
        String refreshToken = jwtUtils.generateTokenFromUsername(user.getEmployeeNumber(), REFRESH_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(user.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        AuthResponse authResponse = authMapper.toDto(user, accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.OK.value())
                .body(responseMapper.toDto(
                        true, "LOGIN SUCCESSFULLY", authResponse, null
                ));
    }

    @Operation(
            summary = "Sign in with Google for customer",
                            description = "Return response show that user has signed in successfully"
    )
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

    @Operation(
            summary = "Verify Username Forgot Password",
                description = "Return response show that verify username forgot password request successfully"
    )
    @PostMapping("/verify-username-forgot-password")
    public ResponseEntity<RestResponse<Object, Object>> verifyForgotPassword(@RequestBody VerifyUsernameForgotPasswordRequest req) throws Exception {
        Map<String, Object> result = authService.verifyUsernameForgotPassword(req.getUsername());
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true, "OTP Sent To Email", result, null
        ));
    }

    @Operation(
            summary = "Verify OTP Forgot Password",
                description = "Return response show thât verify OTP forgot password request successfully"
    )
    @PostMapping("/verify-otp-forgot-password")
    public ResponseEntity<RestResponse<Object, Object>> verifyOtpForgotPassword(@RequestBody VerifyOtpForgotPasswordRequest request) {
        log.info(">>> We are at verifyOtpForgotPassword");
        authService.verifyOtpForgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true, "Verified OTP Successfully", null, null
        ));
    }

    @Operation(
            summary = "Forgot Password API",
                        description = "Return response show that new password is updated"
    )
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

    @Operation(summary = "Change Password API",
                description = "Return response show that new password is updated")
    @PostMapping("/change-password")
    public ResponseEntity<RestResponse<BuyerResponse, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) throws Exception {
        Buyer buyer = authService.changePassword(request);
        BuyerResponse responseData = buyerMapper.toDto(buyer);
        return ResponseEntity.status(HttpStatus.OK.value()).body(
                responseMapper.toDto(
                        true,
                        "CHANGE PASSWORD SUCCESSFULLY",
                        responseData,
                        null)
        );
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Verify otp via email",
                description = "Return verify email."
    )
    public ResponseEntity<RestResponse<AuthResponse, Object>> verify(@Valid @RequestBody VerifyOtpRequest req) {
        Buyer buyer = signUpService.verifyOtp(req);
        String refreshToken = jwtUtils.generateTokenFromUsername(buyer.getUsername(), REFRESH_EXPIRE_TIME);
        String accessToken = jwtUtils.generateTokenFromUsername(buyer.getUsername(), ACCESS_EXPIRE_TIME);
        redisTokenService.saveTokenToRedis(buyer.getEmail(), refreshToken, REFRESH_EXPIRE_TIME);

        AuthResponse authResponse = authMapper.toDto(buyer, accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK.value()).body(
                responseMapper.toDto(
                true,
                        "SIGN UP SUCCESSFULLY",
                        authResponse,
                        null
                )
        );
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Issues new access token when old access token is expired.",
            description = "When access token is expired, Front end send refresh token to this endpoint and receive new access token"
    )
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        log.info(">>> [Refresh token controller]: {}", request.getHeader("Authorization"));
        try {
            Map<String, Object> data = authService.refreshToken(request);
            String email, username;
            Admin admin = null;
            Buyer buyer = null;

            if(data.get("admin") != null) {
                admin = (Admin) data.get("admin");
                email = admin.getEmail();
                username = admin.getEmployeeNumber();
            } else {
                buyer = (Buyer) data.get("buyer");
                email =  buyer.getEmail();
                username = buyer.getUsername();
            }

            log.info(">>> [User email]: {}", email);
            log.info(">>> [Username]: {}", username);
            String savedToken = redisTokenService.getRefreshToken(email);
            String token = (String) data.get("refresh_token");

            if(redisTokenService.verifyRefreshToken(email) &&
                    savedToken.equalsIgnoreCase(token)) {

                String newAccessToken = jwtUtils.generateTokenFromUsername(username, ACCESS_EXPIRE_TIME);
                String newRefreshToken = jwtUtils.generateTokenFromUsername(username, REFRESH_EXPIRE_TIME);
                redisTokenService.deleteRefreshToken(email);
                redisTokenService.saveTokenToRedis(email, newRefreshToken, REFRESH_EXPIRE_TIME);

                return ResponseEntity.ok(responseMapper.toDto(
                        true,
                        "GET NEW TOKEN SUCCESSFULLY.",
                        authMapper.toDto(username, email, newAccessToken, newRefreshToken),
                        null
                ));
            }else {
                return ResponseEntity.badRequest().body(responseMapper.toDto(
                        false,
                        "INVALID REFRESH TOKEN",
                        null, null
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(responseMapper.toDto(
                    false,
                    "ERROR OCCUR WHEN GET NEW TOKENS.",
                    null, e
            ));
        }
    }
}
