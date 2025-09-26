package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.Mapper.BuyerMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.response.SignUpResponse;
import Green_trade.green_trade_platform.service.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthServiceImpl service;

    @Autowired
    private BuyerMapper mapper;

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUpController(@RequestBody UsernamePasswordSignUpRequest request) {
        Buyer buyer = service.signUp(request);
        return ResponseEntity.ok(mapper.toDto(buyer));
    }
}
