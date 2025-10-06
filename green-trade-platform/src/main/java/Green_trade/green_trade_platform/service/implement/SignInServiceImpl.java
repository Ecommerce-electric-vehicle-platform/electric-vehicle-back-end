package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.AuthException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.SignInGoogleRequest;
import Green_trade.green_trade_platform.request.SignInRequest;
import Green_trade.green_trade_platform.service.SignInService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;

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
    private GoogleVerifierServiceImpl googleVerifier;

    public Buyer startSignIn(SignInRequest request) {
        try {
            log.info("startSignIn of SignInServiceImpl: started");
            String username = request.getUsername();
            String password = request.getPassword();

            Optional<Buyer> buyerOpt = buyerRepository.findByUsername(username);
            if (buyerOpt.isEmpty() || !passwordEncoder.matches(password, buyerOpt.get().getPassword())) {
                log.info("startSignIn at SignInServiceImpl: user: {} authenticated failed", username);
                throw new AuthException("Username/password is incorrect");
            }
            log.info("startSignIn at SignInServiceImpl: user: {} authenticated successfully", username);
            log.info("startSignIn of SignInServiceImpl: ended");
            return buyerOpt.get();
        } catch (Exception e) {
            log.info("startSignIn of SignServiceImpl: Error occurred");
            log.info("startSignIn of SignInServiceImpl: ended");
            throw e;
        }
    }

    @Override
    public Buyer startSignInWithGoogle(SignInGoogleRequest body) throws Exception {
        try {
            log.info("startSignInWithGoogle of GoogleVerifierService: started");
            String idToken = body.getIdToken();
            GoogleIdToken.Payload googleUserData = googleVerifier.verify(idToken);

            String email = googleUserData.getEmail();
            log.info("startSignInWithGoogle of GoogleVerifierService: user with email: {}", email);

            Optional<Buyer> buyerOpt = buyerRepository.findByEmail(email);

            if (buyerOpt.isEmpty()) {
                log.info("startSignInWithGoogle of GoogleVerifierService: New User with email: {}", email);
                String username = googleUserData.getEmail().split("@")[0];
                String password = passwordEncoder.encode(UUID.randomUUID().toString());
                Buyer user = Buyer.builder()
                        .username(username)
                        .password(password)
                        .email(email)
                        .build();
                return buyerRepository.save(user);
            }
            log.info("startSignInWithGoogle of GoogleVerifierService: end");
            return buyerOpt.get();
        } catch (Exception e) {
            log.info("startSignInWithGoogle of GoogleVerifierService: Error occurred:" + e);
            throw new Exception("Sign In With Google Failed");
        }
    }
}
