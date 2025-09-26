package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.Mapper.BuyerMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.UsernamePasswordSignUpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AuthServiceImpl implements AuthService{
    @Autowired
    private BuyerRepository repository;

    @Autowired
    private BuyerMapper mapper;

    @Autowired
    private DelegatingPasswordEncoder passwordEncoder;

    @Override
    public Buyer signUp(UsernamePasswordSignUpRequest request) {
        Buyer buyer = mapper.toEntity(request);
        // Hash password
        buyer.setPassword(passwordEncoder.encode(buyer.getPassword()));
        return repository.save(buyer);
    }
}
