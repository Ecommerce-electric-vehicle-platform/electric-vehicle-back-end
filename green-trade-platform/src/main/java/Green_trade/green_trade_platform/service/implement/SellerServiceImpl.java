package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SubscriptionMapper;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.repository.SellerRepository;
import Green_trade.green_trade_platform.repository.SubscriptionRepository;
import Green_trade.green_trade_platform.response.SubscriptionResponse;
import Green_trade.green_trade_platform.service.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SellerServiceImpl implements SellerService {

    private SellerRepository sellerRepository;

    private SubscriptionRepository subscriptionRepository;

    private SubscriptionMapper subscriptionMapper;

    public SellerServiceImpl(
            SellerRepository sellerRepository,
            SubscriptionRepository subscriptionRepository,
            SubscriptionMapper subscriptionMapper,
            ResponseMapper responseMapper) {
        this.sellerRepository = sellerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
    }
    public SubscriptionResponse checkServicePackageValidity(Long id) throws Exception {
        try {
            Optional<Seller> sellerOpt = sellerRepository.findById(id);
            if(sellerOpt.isEmpty()) {
                throw new Exception("Seller is not existed");
            }

            Subscription subscription = subscriptionRepository.findByIdOrderByEndDayDesc(id);

            if(LocalDateTime.now().isAfter(subscription.getEndDay())) {
                throw new Exception("Subscription is expired");
            }

            return subscriptionMapper.toDto(true, subscription.getEndDay(), "");
        } catch (Exception e) {
            log.info("Error at checkServicePackageValidity: {}", e);
            throw e;
        }
    }
}
