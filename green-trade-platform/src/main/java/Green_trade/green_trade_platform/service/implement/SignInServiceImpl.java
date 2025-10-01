package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.AuthException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.SignInRequest;
import Green_trade.green_trade_platform.service.SignInService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SignInServiceImpl implements SignInService {
    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private DelegatingPasswordEncoder passwordEncoder;

    public Buyer startSignIn(SignInRequest request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();

            Optional<Buyer> buyerOpt = buyerRepository.findByUsername(username);
            log.info("isBuyeEmpty: {}", buyerOpt.isEmpty());
            log.info("isPasswordMatched: {}", passwordEncoder.matches(password, buyerOpt.get().getPassword()));
            if(buyerOpt.isEmpty() || !passwordEncoder.matches(password, buyerOpt.get().getPassword())) {
                throw new AuthException("Username/password is incorrect");
            }
            return buyerOpt.get();
        } catch (Exception e) {
            throw e;
        }
    }
}
