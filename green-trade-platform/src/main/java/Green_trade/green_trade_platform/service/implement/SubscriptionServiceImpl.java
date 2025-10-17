package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.repository.SubscriptionRepository;
import Green_trade.green_trade_platform.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    private SubscriptionServiceImpl(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
    }
    public boolean isServicePackageExpired(Long sellerId) throws Exception {
        boolean result = false;
        Subscription subscription = subscriptionRepository.findBySeller_SellerIdOrderByEndDayDesc(sellerId)
                .orElseThrow(
                        () -> new Exception("Seller doesn't subscribe any service")
                );
        if(LocalDateTime.now().isAfter(subscription.getEndDay())) {
            result = true;
        }
        return result;
    }
}
