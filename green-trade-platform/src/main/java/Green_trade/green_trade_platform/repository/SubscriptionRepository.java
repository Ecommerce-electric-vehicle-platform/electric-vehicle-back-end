package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Subscription findBySeller_SellerIdOrderByEndDayDesc(Long sellerId);
}
