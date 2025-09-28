package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceCustomer implements UserDetailsService {
    @Autowired
    private BuyerRepository buyerRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check buyer table
        Optional<Buyer> buyer = buyerRepo.findByUsername(username);
        if (buyer.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                    buyer.get().getUsername(),
                    buyer.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
            );
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
