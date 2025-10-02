package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.AuthException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.SignInRequest;
import Green_trade.green_trade_platform.service.SignInService;
import Green_trade.green_trade_platform.util.GoogleVerifierService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class SignInServiceImpl implements SignInService {
    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private DelegatingPasswordEncoder passwordEncoder;

    @Autowired
    private GoogleVerifierService googleVerifier;

    public Buyer startSignIn(SignInRequest request) {
        try {
            log.debug("startSignIn of SignInServiceImpl: started");
            String username = request.getUsername();
            String password = request.getPassword();

            Optional<Buyer> buyerOpt = buyerRepository.findByUsername(username);
            if(buyerOpt.isEmpty() || !passwordEncoder.matches(password, buyerOpt.get().getPassword())) {
                log.debug("startSignIn at SignInServiceImpl: user: {} authenticated failed", username);
                throw new AuthException("Username/password is incorrect");
            }
            log.debug("startSignIn at SignInServiceImpl: user: {} authenticated successfully", username);
            log.debug("startSignIn of SignInServiceImpl: ended");
            return buyerOpt.get();
        } catch (Exception e) {
            log.error("startSignIn of SignServiceImpl: Error occurred");
            log.error("startSignIn of SignInServiceImpl: ended");
            throw e;
        }
    }

    @Override
    public Buyer startSignInWithGoogle(Map<String, String> body) throws Exception {
        try {
            String idToken = body.get("idToken");
            GoogleIdToken.Payload googleUserData = googleVerifier.verify(idToken);

            String email = googleUserData.getEmail();

            Optional<Buyer> buyerOpt = buyerRepository.findByEmail(email);

            if(buyerOpt.isEmpty()) {
                String username = googleUserData.getEmail().split("@")[0];
                String password = passwordEncoder.encode(UUID.randomUUID().toString());
                Buyer user = Buyer.builder()
                        .username(username)
                        .password(password)
                        .email(email)
                        .build();
                buyerRepository.save(user);
            }

            return buyerOpt.get();
        } catch (Exception e) {
            throw new Exception("Sign In With Google Failed");
        }
    }
}
