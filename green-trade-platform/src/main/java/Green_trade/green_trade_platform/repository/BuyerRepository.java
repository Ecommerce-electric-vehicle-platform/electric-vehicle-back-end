package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    Optional<Buyer> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Buyer> findByEmail(String email);
}
