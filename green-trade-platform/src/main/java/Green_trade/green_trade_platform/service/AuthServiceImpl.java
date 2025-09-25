package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.Mapper.BuyerMapper;
import Green_trade.green_trade_platform.exception.InvalidArgumentException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import Green_trade.green_trade_platform.util.Acceptable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AuthServiceImpl implements AuthService{
    @Autowired
    private BuyerRepository repository;

    @Autowired
    private Acceptable acceptable;

    @Autowired
    private BuyerMapper mapper;

    public void checkValidData(UsernamePasswordSignUpRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Validate username
        if (request.getUsername() == null || !acceptable.isValid(request.getUsername(), acceptable.USERNAME_REGEX)) {
            errors.put("username", "Username must be at least 8 letters, with no spaces, numbers, or special characters.");
        }

        if(request.getPassword() == null || !acceptable.isValid(request.getPassword(), acceptable.PASSWORD_REGEX)) {
            errors.put("password", "Password must be at least 8 characters, include letters, numbers, and special characters, and contain no spaces.");
        }

        if(request.getPhoneNumber() == null || !acceptable.isValid(request.getPhoneNumber(), acceptable.PHONE_NUMBER_REGEX)) {
            errors.put("phone number", "Invalid phone number.");
        }

        if(request.getEmail() == null || !acceptable.isValid(request.getEmail(), acceptable.EMAIL_REGEX)) {
            errors.put("email", "Invalid email format.");
        }

        if(!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    @Override
    public Buyer signUp(UsernamePasswordSignUpRequest request) {
        checkValidData(request);

        Buyer buyer = mapper.toEntity(request);
        return repository.save(buyer);
    }
}
