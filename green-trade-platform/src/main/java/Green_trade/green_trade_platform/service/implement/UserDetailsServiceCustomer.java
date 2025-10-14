package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceCustomer implements UserDetailsService {
    private final BuyerRepository buyerRepo;
    private final SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check buyer table
        Optional<Buyer> buyer = buyerRepo.findByUsername(username);
        if (buyer.isPresent()) {
            Optional<Seller> seller = sellerRepository.findByBuyer(buyer.get());
            if(seller.isPresent()) {
                return new org.springframework.security.core.userdetails.User(
                        buyer.get().getUsername(),
                        buyer.get().getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
                );
            }

            return new org.springframework.security.core.userdetails.User(
                    buyer.get().getUsername(),
                    buyer.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
            );
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
